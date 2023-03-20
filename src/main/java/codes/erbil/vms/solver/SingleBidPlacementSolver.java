package codes.erbil.vms.solver;

import codes.erbil.vms.config.model.*;
import codes.erbil.vms.entity.TestSolution;
import gurobi.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SingleBidPlacementSolver {

    @ConfigProperty(name = "vms.solver.sbp.timelimit")
    Long sbpTimeLimit;

    @ConfigProperty(name = "vms.solver.sbp.batchsize")
    Long sbpBatchSize;

    @ConfigProperty(name = "vms.solver.sbp.thread.limit", defaultValue = "1")
    int singleCaseThreadLimit;

    public TestSolution solve(TestCase tc, List<Integer> orderedBidIndexes, boolean isZandYFixed, String order) {
        Map<String, List<Integer>> omega = tc.getOmega();

        TestSolution ts = new TestSolution();
        ts.setFeasible(true);
        ts.setOptimal(false);

        Instant start = Instant.now();
        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", tc.getTestCaseName());
            env.start();

            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, tc.getTestCaseName());

            // CREATE DECISION VARIABLES
            GRBVar[] x = model.addVars(tc.getBids().size() + 1, GRB.BINARY);
            GRBVar[][] y = new GRBVar[tc.getBids().size() + 1][tc.getPeriod() + 1];
            GRBVar[][] o = new GRBVar[tc.getPhysicalMachines().size() + 1][tc.getPeriod() + 1];

            // z[a][t][i][j][k]
            Map<Integer, //a
                    Map<Integer, //t
                            Map<Integer, //i
                                    Map<Integer, //j
                                            Map<Integer, // k
                                                    GRBVar>>>>> z = new HashMap<>(tc.getPhysicalMachines().size() + 1);


            Map<Integer, List<GRBVar>> zByBidIndexes = prepareGurobiModel(tc, omega, model, x, y, o, z);

            model.set(GRB.DoubleParam.TimeLimit, sbpTimeLimit);
            model.set(GRB.IntParam.Threads, singleCaseThreadLimit);
            model.set(GRB.DoubleParam.MIPGap, 1e-2);
            model.set(GRB.IntParam.Presolve, 0);
            model.update();
            // model.write("./" + tc.getTestCaseName() + ".lp");

            // ASIL ÇÖZÜMÜN UYGULANDIĞI YER
            List<Integer> indexesToBeFixed = new ArrayList<>();
            for (Integer bidIndex : orderedBidIndexes) {
                x[bidIndex].set(GRB.DoubleAttr.UB, 1.0);
                performEnableOperationForZandY(tc, y, zByBidIndexes, bidIndex);

                model.update();
                model.optimize();

                int status = model.get(GRB.IntAttr.Status);


                if ((status == GRB.INF_OR_UNBD) || // EĞER SONUÇ INFEASIBLE VEYA UNBOUNDED ise UB tekrar 0 yapılır.
                        (status == GRB.INFEASIBLE) ||
                        (status == GRB.UNBOUNDED) ||
                        (status == GRB.NUMERIC)) {

                    System.out.println("The model cannot be solved with this bid: " + bidIndex +
                            " because it is infeasible or unbounded. STATUS:" + status);
                    x[bidIndex].set(GRB.DoubleAttr.UB, 0.0);
                    performDisableOperationForZandY(tc, y, zByBidIndexes, bidIndex);
                    continue; // Bir sonraki bide geçilir.
                }

                // Eğer bid yerleştirildiyse (feasible ise) LB 1 eşitle
                if (x[bidIndex].get(GRB.DoubleAttr.X) >= 0.99) {
                    x[bidIndex].set(GRB.DoubleAttr.LB, x[bidIndex].get(GRB.DoubleAttr.X));

                    // z ve y değişkenleri flaglere göre sabitlenecek
                    if (isZandYFixed) {
                        performFixOperationForZandY(tc, y, zByBidIndexes, bidIndex);
                    } else {
                        indexesToBeFixed.add(bidIndex);

                        // Belirli bir bid sayısına ulaşınca fixle
                        if (indexesToBeFixed.size() == sbpBatchSize) {
                            System.out.println("Batch doldu. İlgili bidlere ait y ve z değişkenleri fixleniyor...");

                            for (Integer bi : indexesToBeFixed) {
                                performFixOperationForZandY(tc, y, zByBidIndexes, bi);
                            }

                            indexesToBeFixed.clear();
                        }
                    }

                } else { // Eğer bid yerleştirilmediyse UB tekrar sıfırla
                    x[bidIndex].set(GRB.DoubleAttr.UB, 0.0);
                    performDisableOperationForZandY(tc, y, zByBidIndexes, bidIndex);
                }
            }

            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toSeconds();


            Map<String, Double> solutionDecisionVariableMap = new HashMap<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) > 0.99) {
                    System.out.println(var.get(GRB.StringAttr.VarName) + ":" + var.get(GRB.DoubleAttr.X));
                    solutionDecisionVariableMap.put(var.get(GRB.StringAttr.VarName), var.get(GRB.DoubleAttr.X));
                }
            }

            ts.setObjectiveValue(model.get(GRB.DoubleAttr.ObjVal));

            if (isZandYFixed) {
                ts.setSolutionStrategy("SBPF-" + order);
            } else {
                ts.setSolutionStrategy("SBPN-" + order);
            }

            ts.setTimeElapsedSeconds(timeElapsed);
            ts.setMipGap(model.get(GRB.DoubleAttr.MIPGap));
            ts.setDecisionVariables(solutionDecisionVariableMap);

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
            ts.getErrors().add(e.getMessage() + ", Error Code:" + e.getErrorCode());
        }

        return ts;
    }

    private void performFixOperationForZandY(TestCase tc, GRBVar[][] y, Map<Integer, List<GRBVar>> zByBidIndexes, Integer bidIndex) throws GRBException {
        for (GRBVar zByIdx : zByBidIndexes.get(bidIndex)) {
            if (zByIdx.get(GRB.DoubleAttr.X) >= 0.99) {
                zByIdx.set(GRB.DoubleAttr.LB, zByIdx.get(GRB.DoubleAttr.X));
            }
        }

        for (int t = 1; t <= tc.getPeriod(); t++) {
            if (y[bidIndex][t].get(GRB.DoubleAttr.X) >= 0.99) {
                y[bidIndex][t].set(GRB.DoubleAttr.LB, y[bidIndex][t].get(GRB.DoubleAttr.X));
            }
        }
    }

    private void performEnableOperationForZandY(TestCase tc, GRBVar[][] y, Map<Integer, List<GRBVar>> zByBidIndexes, Integer bidIndex) throws GRBException {
        for (GRBVar zByIdx : zByBidIndexes.get(bidIndex)) {
            zByIdx.set(GRB.DoubleAttr.UB, Double.POSITIVE_INFINITY);
        }

        for (int t = 1; t <= tc.getPeriod(); t++) {
            y[bidIndex][t].set(GRB.DoubleAttr.UB, 1.0);
        }
    }

    private void performDisableOperationForZandY(TestCase tc, GRBVar[][] y, Map<Integer, List<GRBVar>> zByBidIndexes, Integer bidIndex) throws GRBException {
        for (GRBVar zByIdx : zByBidIndexes.get(bidIndex)) {
            zByIdx.set(GRB.DoubleAttr.UB, 0.0);
        }

        for (int t = 1; t <= tc.getPeriod(); t++) {
            y[bidIndex][t].set(GRB.DoubleAttr.UB, 0.0);
        }
    }

    private Map<Integer, List<GRBVar>> prepareGurobiModel(TestCase tc, Map<String, List<Integer>> omega, GRBModel model, GRBVar[] x,
                                                          GRBVar[][] y, GRBVar[][] o,
                                                          Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, GRBVar>>>>> z)
            throws GRBException {

        Map<Integer, List<GRBVar>> zByBidIndex = new HashMap<>();

        for (int i = 1; i <= tc.getBids().size(); i++) {
            x[i].set(GRB.StringAttr.VarName, "x[" + i + "]");

            // BU ÇÖZÜME GÖRE BÜTÜN DEĞİŞKENLER KAPALI BAŞLAYACAK
            x[i].set(GRB.DoubleAttr.UB, 0.0);
        }

        for (int i = 1; i <= tc.getBids().size(); i++) {
            y[i] = model.addVars(tc.getPeriod() + 1, GRB.BINARY);

            for (int t = 1; t <= tc.getPeriod(); t++) {
                y[i][t].set(GRB.StringAttr.VarName, "y[i=" + i + "][t=" + t + "]");

                // BU ÇÖZÜME GÖRE BÜTÜN DEĞİŞKENLER KAPALI BAŞLAYACAK
                y[i][t].set(GRB.DoubleAttr.UB, 0.0);
            }
        }

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            o[a] = model.addVars(tc.getPeriod() + 1, GRB.BINARY);

            for (int t = 1; t <= tc.getPeriod(); t++) {
                o[a][t].set(GRB.StringAttr.VarName, "o[a=" + a + "][t=" + t + "]");
            }
        }

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            z.put(a, new HashMap<>(tc.getPeriod()));

            for (int t = 1; t <= tc.getPeriod(); t++) {
                z.get(a).put(t, new HashMap<>(tc.getBids().size()));

                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);
                    z.get(a).get(t).put(i, new HashMap<>(bid.getSubbidList().size()));

                    for (int j = 1; j <= tc.getBids().get(i - 1).getSubbidList().size(); j++) {
                        Subbid subbid = tc.getBids().get(i - 1).getSubbidList().get(j - 1);
                        z.get(a).get(t).get(i).put(j, new HashMap<>(subbid.getVmAlternatives().size()));

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            String st = "z[a=" + a + "][t=" + t + "][i=" + i + "][j=" + j + "][k=" + k + "]";

                            //z.get(a).get(t).get(i).get(j).put(k, model.addVar(0.0, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, st));
                            // BU ÇÖZÜME GÖRE BÜTÜN DEĞİŞKENLER KAPALI BAŞLAYACAK
                            z.get(a).get(t).get(i).get(j).put(k, model.addVar(0.0, 0.0, 1.0, GRB.INTEGER, st));

                            zByBidIndex.computeIfAbsent(i, l -> new ArrayList<>())
                                    .add(z.get(a).get(t).get(i).get(j).get(k));
                        }
                    }
                }
            }
        }

        // CREATE OBJECTIVE FUNCTION
        GRBLinExpr obj1 = new GRBLinExpr();

        for (int i = 1; i <= tc.getBids().size(); i++) {
            obj1.addTerm(tc.getBids().get(i - 1).getPrice().doubleValue(), x[i]);
        }

        GRBLinExpr obj2 = new GRBLinExpr();

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            for (int t = 1; t <= tc.getPeriod(); t++) {
                Double idleCost = tc.getPhysicalMachines().get(a - 1).getPhysicalServerConfig().getIdleCost();

                obj2.addTerm(idleCost, o[a][t]);
            }
        }

        GRBLinExpr obj3 = new GRBLinExpr();
        GRBLinExpr obj4 = new GRBLinExpr();

        for (int t = 1; t <= tc.getPeriod(); t++) {
            for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
                PhysicalMachine pm = tc.getPhysicalMachines().get(a - 1);

                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);
                    int earliest = tc.getBids().get(i - 1).getEarliestTime();
                    int latest = tc.getBids().get(i - 1).getLatestTime();
                    int duration = tc.getBids().get(i - 1).getDuration();

                    if (t < earliest || t > latest) {
                        continue;
                    }


                    for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                        Subbid subbid = bid.getSubbidList().get(j - 1);

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                            if (!vmAlternative.getVmType().equalsIgnoreCase(pm.getConfiguredForVmType().getVmTypeId())) {
                                continue;
                            }

                            Double e_full_minus_idle = pm.getPhysicalServerConfig().getFullLoadCost() - pm.getPhysicalServerConfig().getIdleCost();

                            for (int t_prime = Math.max(earliest, t - duration + 1); t_prime <= Math.min(latest - duration + 1, t); t_prime++) {
                                // 3RD Component of the Obj. Function
                                Double coeff3 = (e_full_minus_idle * vmAlternative.getVmSize()) / pm.getUA();
                                obj3.addTerm(coeff3, z.get(a).get(t_prime).get(i).get(j).get(k));

                                // 4TH Component of the Obj. Function
                                Double p_res = pm.getConfiguredForVmType().getReservationPrice() * vmAlternative.getVmSize();
                                obj4.addTerm(p_res, z.get(a).get(t_prime).get(i).get(j).get(k));
                            }
                        }
                    }
                }
            }
        }

        // ###### CREATE OBJECTIVE FUNCTION #####################
        obj2.add(obj3); // add cost
        obj2.add(obj4); // add cost
        obj1.multAdd(-1, obj2); // remove cost from the revenue

        model.setObjective(obj1, GRB.MAXIMIZE);

        // CREATE CONSTRAINTS
        // Eq #2
        for (int i = 1; i <= tc.getBids().size(); i++) {
            Bid bid = tc.getBids().get(i - 1);
            int sumEnd = bid.getLatestTime() - bid.getDuration() + 1;

            GRBLinExpr eq2 = new GRBLinExpr();

            for (int t = bid.getEarliestTime(); t <= sumEnd; t++) {
                eq2.addTerm(1, y[i][t]);
            }

            eq2.addTerm(-1, x[i]);
            model.addConstr(eq2, GRB.EQUAL, 0, "Eq2[i=" + i + "]");
        }

        // Eq #3
        for (int i = 1; i <= tc.getBids().size(); i++) {
            Bid bid = tc.getBids().get(i - 1);
            int earliest = tc.getBids().get(i - 1).getEarliestTime();
            int latest = tc.getBids().get(i - 1).getLatestTime();
            int duration = tc.getBids().get(i - 1).getDuration();

            for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                Subbid subbid = bid.getSubbidList().get(j - 1);

                for (int t = earliest; t <= latest - duration + 1; t++) {
                    GRBLinExpr eq3 = new GRBLinExpr();

                    for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                        SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                        // If that VM Type has not mapped by any Physical machine
                        if (!omega.containsKey(vmAlternative.getVmType())) {
                            continue;
                        }

                        for (Integer a : omega.get(vmAlternative.getVmType())) {
                            eq3.addTerm(1, z.get(a).get(t).get(i).get(j).get(k));
                        }
                    }


                    eq3.addTerm(-1 * subbid.getQuantity(), y[i][t]);

                    String st = "Eq3[i=" + i + "][j=" + j + "][t=" + t + "]";
                    model.addConstr(eq3, GRB.EQUAL, 0, st);
                }
            }
        }

        // Eq #4
        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            PhysicalMachine pm = tc.getPhysicalMachines().get(a - 1);

            for (int t = 1; t <= tc.getPeriod(); t++) {
                GRBLinExpr eq4Lhs = new GRBLinExpr();
                GRBLinExpr eq4Rhs = new GRBLinExpr();

                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);
                    int earliest = tc.getBids().get(i - 1).getEarliestTime();
                    int latest = tc.getBids().get(i - 1).getLatestTime();
                    int duration = tc.getBids().get(i - 1).getDuration();

                    if (t < earliest || t > latest) {
                        continue;
                    }

                    for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                        Subbid subbid = bid.getSubbidList().get(j - 1);

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                            if (!vmAlternative.getVmType().equalsIgnoreCase(pm.getConfiguredForVmType().getVmTypeId())) {
                                continue;
                            }

                            for (int t_prime = Math.max(earliest, t - duration + 1); t_prime <= Math.min(latest - duration + 1, t); t_prime++) {
                                eq4Lhs.addTerm(vmAlternative.getVmSize(), z.get(a).get(t_prime).get(i).get(j).get(k));
                            }
                        }
                    }
                }

                eq4Rhs.addTerm(pm.getUA(), o[a][t]);
                String st = "Eq4[a=" + a + "][t=" + t + "]";
                model.addConstr(eq4Lhs, GRB.LESS_EQUAL, eq4Rhs, st);
            }
        }

        return zByBidIndex;
    }
}
