package codes.erbil.vms.solver;

import codes.erbil.vms.config.model.*;
import codes.erbil.vms.entity.TestSolution;
import gurobi.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EcoSchedGurobiSolver {

    @ConfigProperty(name = "vms.solver.gurobi.timelimit")
    Long timeLimit;

    @ConfigProperty(name = "vms.solver.gurobi.thread.limit", defaultValue = "1")
    int singleCaseThreadLimit;

    public TestSolution solve(TestCase tc) {
        Map<String, List<Integer>> omega = tc.getOmega();

        TestSolution ts = new TestSolution();
        ts.setFeasible(true);

        Instant start = Instant.now();
        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", tc.getTestCaseName());
            env.start();

            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, tc.getTestCaseName());

            // CREATE DECISION VARIABLES
            GRBVar[] x = model.addVars(tc.getBids().size() + 1, GRB.BINARY);

            for (int i = 1; i <= tc.getBids().size(); i++) {
                x[i].set(GRB.StringAttr.VarName, "x[" + i + "]");
            }

            GRBVar[][] y = new GRBVar[tc.getBids().size() + 1][tc.getPeriod() + 1];
            for (int i = 1; i <= tc.getBids().size(); i++) {
                y[i] = model.addVars(tc.getPeriod() + 1, GRB.BINARY);

                for (int t = 1; t <= tc.getPeriod(); t++) {
                    y[i][t].set(GRB.StringAttr.VarName, "y[i=" + i + "][t=" + t + "]");
                }
            }

            GRBVar[][] o = new GRBVar[tc.getPhysicalMachines().size() + 1][tc.getPeriod() + 1];
            for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
                o[a] = model.addVars(tc.getPeriod() + 1, GRB.BINARY);

                for (int t = 1; t <= tc.getPeriod(); t++) {
                    o[a][t].set(GRB.StringAttr.VarName, "o[a=" + a + "][t=" + t + "]");
                }
            }

            // z[a][t][i][j][k]
            Map<Integer, //a
                    Map<Integer, //t
                            Map<Integer, //i
                                    Map<Integer, //j
                                            Map<Integer, // k
                                                    GRBVar>>>>> z = new HashMap<>(tc.getPhysicalMachines().size() + 1);
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

                                z.get(a).get(t).get(i).get(j).put(k, model.addVar(0.0, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, st));
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

            model.set(GRB.DoubleParam.TimeLimit, timeLimit);
            model.set(GRB.IntParam.Threads, singleCaseThreadLimit);
            model.set(GRB.DoubleParam.MIPGap, 1e-3);
            model.update();
            // model.write("./" + tc.getTestCaseName() + ".lp");

            model.optimize();

            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toSeconds();

            int status = model.get(GRB.IntAttr.Status);

            if ((status == GRB.OPTIMAL)) {
                ts.setOptimal(true);
            } else if ((status == GRB.INF_OR_UNBD) ||
                    (status == GRB.INFEASIBLE) ||
                    (status == GRB.UNBOUNDED) ||
                    (status == GRB.NUMERIC)) {
                ts.setFeasible(false);
                ts.setOptimal(false);
                System.out.println("The model cannot be solved because it is infeasible or unbounded. STATUS:" + status);
            } else { // TIME LIMIT
                ts.setOptimal(false);
            }


            Map<String, Double> solutionDecisionVariableMap = new HashMap<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) > 0.99) {
                    System.out.println(var.get(GRB.StringAttr.VarName) + ":" + var.get(GRB.DoubleAttr.X));
                    solutionDecisionVariableMap.put(var.get(GRB.StringAttr.VarName), var.get(GRB.DoubleAttr.X));
                }
            }

            ts.setObjectiveValue(model.get(GRB.DoubleAttr.ObjVal));
            ts.setSolutionStrategy("GUROBI");
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
}
