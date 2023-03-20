package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.PhysicalServerConfigManager;
import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.config.model.*;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.model.DummyUtilization;
import codes.erbil.vms.solver.model.DummyZVar;
import gurobi.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class GreedySolver {

    @Inject
    Random randomG;

    @ConfigProperty(name = "vms.solver.greedy.relaxation.timelimit")
    Long timeLimit;

    @ConfigProperty(name = "vms.solver.greedy.thread.limit", defaultValue = "1")
    int singleCaseThreadLimit;

    @Inject
    VerifierManager verifierManager;

    // O4 Order - For calculating the energy cost
    @Inject
    PhysicalServerConfigManager physicalServerConfigManager;

    public TestSolution solve(TestCase tc, List<Integer> orderedBidIndexes, String order) {
        Instant start = Instant.now();

        // Physical machine a -> { Time Slot t -> {Utilization Level} }
        Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime = verifierManager.initializeMachineTimeMap(tc);

        // z[a][t][i][j][k]
        Map<Integer, //a
                Map<Integer, //t
                        Map<Integer, //i
                                Map<Integer, //j
                                        Map<Integer, // k
                                                Integer>>>>> z = initializeZVar(tc);

        Map<String, List<Integer>> omega = tc.getOmega();

        TestSolution ts = new TestSolution();
        ts.setFeasible(true);

        Double currentObjVal = 0.00;

        for (Integer i : orderedBidIndexes) {
            Bid bid = tc.getBids().get(i - 1);

            // Even if all sub-bids are placed, it will not be accepted if there is no improvement in the obj. function.
            boolean bidAccepted = false;

            boolean allSubbidsArePlaced = false;
            for (int t = bid.getEarliestTime(); t <= bid.getLatestTime() - bid.getDuration() + 1; t++) {

                List<DummyZVar> dummyZForSubbids = new ArrayList<>();
                List<DummyUtilization> dummyUtilForSubbids = new ArrayList<>();

                boolean currentSubbidsArePlaced = true;
                int j = 1;
                for (Subbid sb : bid.getSubbidList()) {


                    int remainingQty = sb.getQuantity();
                    boolean subbidPlaced = false;

                    List<DummyZVar> dummyZForVmAlternatives = new ArrayList<>();
                    List<DummyUtilization> dummyUtilForVmAlternatives = new ArrayList<>();


                    // All vm alternatives are traversed. (ilk sırada zaten en ucuz res. price'a sahip olan gelecek)
                    int k = 1;
                    for (SubbidCtx sbc : sb.getVmAlternatives()) {

                        // VM'i çalıştırmaya yapılandırılmış fiziksel sunucular gezilir.
                        // All physical servers configured to run the VM are traversed.
                        for (Integer a : omega.get(sbc.getVmType())) {
                            PhysicalMachine pm = tc.getPhysicalMachines().get(a - 1);


                            // Quantity sıfırlanana kadar ya da bütün VM alternatifleri için
                            // Bütün fiziksel sunucular  gezildiğinde bitecek döngü
                            while (remainingQty > 0) {
                                if (remainingQty == 0) {
                                    subbidPlaced = true;
                                    break;
                                }

                                boolean vmAlternativePlaced = true;
                                // Starts from t to duration.
                                // There must be enough space in each time slot. Her bir time slotta yeterli yer bulunması gerekiyor.
                                for (int t_prime = t; t_prime <= t + bid.getDuration() - 1; t_prime++) {
                                    // There is no space. Eklenebilir yer yok
                                    if (sbc.getVmSize() > pm.getUA() - utilizationOfMachineAtTime.get(a).get(t_prime)) {
                                        vmAlternativePlaced = false;
                                        break;
                                    }
                                }

                                // If it fits, update the decision variables. Yerlestirilebilirse ilgili degiskenleri guncelle
                                if (vmAlternativePlaced) {
                                    // Z değişkenine
                                    z.get(a).get(t).get(i).get(j)
                                            .replace(k, z.get(a).get(t).get(i).get(j).get(k) + 1);

                                    // Utilization guncelle ve geri almak için dummy listeye ekle
                                    for (int t_prime = t; t_prime <= t + bid.getDuration() - 1; t_prime++) {
                                        utilizationOfMachineAtTime.get(a).replace(t_prime,
                                                utilizationOfMachineAtTime.get(a).get(t_prime) + sbc.getVmSize());

                                        dummyUtilForVmAlternatives.add(new DummyUtilization(a, t_prime, sbc.getVmSize()));
                                    }


                                    // Eger bütün quantity'ler yerleştirilemezse buradaki bütün değişkenler
                                    // 1 azaltılarak eski haline döndürülür.
                                    //
                                    dummyZForVmAlternatives.add(new DummyZVar(a, t, i, j, k));
                                    remainingQty--;
                                } else { // Yerlestirilmediyse bir sonraki fiziksel sunucuya geç
                                    break;
                                }
                            }
                        }

                        if (remainingQty == 0) {
                            subbidPlaced = true;
                            break;
                        }

                        k++;
                    }


                    if (subbidPlaced) {
                        // Z ve UTIL degiskenlerine ilgili yerlestirme yapilir (Subbid icindeki alternatif VMler icin)
                        dummyZForSubbids.addAll(dummyZForVmAlternatives);
                        dummyUtilForSubbids.addAll(dummyUtilForVmAlternatives);

                    } else {
                        // VM Alternatifleri için yapılan değişiklikler geri alınır

                        degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForVmAlternatives, dummyUtilForVmAlternatives);

                        // 1 Subbidin yerleştirilememesi bile
                        // bid'i satisfy etmediği için geri kalan subbidlere bakmaya gerek kalmaz.
                        // Bir sonraki t değeri için denenir

                        currentSubbidsArePlaced = false;
                    }

                    if (!currentSubbidsArePlaced) {
                        // Önceki subbidlerde yerleştirilenleri geri alır
                        // undos all operations
                        degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForSubbids, dummyUtilForSubbids);

                        dummyZForSubbids = new ArrayList<>();
                        dummyUtilForSubbids = new ArrayList<>();
                        break;
                    }

                    if (currentSubbidsArePlaced && j == bid.getSubbidList().size()) {
                        allSubbidsArePlaced = true;
                    }

                    j++;
                }


                if (allSubbidsArePlaced) {
                    // Obj function hesaplanır. Eğer öncekinden düşük çıkarsa değişiklikler geri alınır.
                    // Bir sonraki t için denenir.
                    // Calculates the obj. function. 
                    // If it is lower than the current solution, the new solution is not accepted.
                    TestSolution dummySolution = new TestSolution();
                    dummySolution.setSolutionStrategy("DUMMY_GREEDY");
                    Map<String, Double> dummyDecVar = prepareDecisionVariables(z, tc);
                    dummySolution.setDecisionVariables(dummyDecVar);

                    Double calculatedObjVal = verifierManager.getObjValue(tc, dummySolution);

                    // The bid is marked as accepted. Bid kabul edilir
                    if (calculatedObjVal > currentObjVal) {
                        ts.setDecisionVariables(dummyDecVar);

                        currentObjVal = calculatedObjVal;
                        bidAccepted = true;
                    } else {
                        // It is not accepted. Undos all operations. Kabul edilmez geri alınır.
                        degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForSubbids, dummyUtilForSubbids);
                    }

                }

                if (bidAccepted) break;
            }
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toSeconds();

        ts.setObjectiveValue(currentObjVal);
        ts.setSolutionStrategy("GREEDY-" + order);
        ts.setTimeElapsedSeconds(timeElapsed);

        return ts;
    }

    public Map<String, Double> prepareDecisionVariables(Map<Integer,
            Map<Integer,
                    Map<Integer,
                            Map<Integer,
                                    Map<Integer, Integer>>>>> z,
                                                        TestCase tc) {

        Map<String, Double> decisionVariables = new HashMap<>();
        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            for (int t = 1; t <= tc.getPeriod(); t++) {

                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);

                    for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                        Subbid subbid = bid.getSubbidList().get(j - 1);

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            Integer zVal = z.get(a).get(t).get(i).get(j).get(k);
                            if (zVal > 0) {
                                decisionVariables.put("z[a=" + a + "][t=" + t + "][i=" + i + "][j=" + j + "][k=" + k + "]", Double.valueOf(zVal));


                                for (int t_prime = t; t_prime <= t + bid.getDuration() - 1; t_prime++) {
                                    if (!decisionVariables.containsKey("o[a=" + a + "][t=" + t_prime + "]")) {
                                        decisionVariables.put("o[a=" + a + "][t=" + t_prime + "]", Double.valueOf(1.0));
                                    }
                                }


                                if (!decisionVariables.containsKey("x[" + i + "]")) {
                                    decisionVariables.put("x[" + i + "]", Double.valueOf(1.0));
                                }

                                if (!decisionVariables.containsKey("y[i=" + i + "][t=" + t + "]")) {
                                    decisionVariables.put("y[i=" + i + "][t=" + t + "]", Double.valueOf(1.0));
                                }
                            }
                        }
                    }
                }
            }
        }

        return decisionVariables;
    }

    public void degiskenleriGeriAl(Map<Integer,
            Map<Integer,
                    Map<Integer,
                            Map<Integer,
                                    Map<Integer, Integer>>>>> z,
                                    Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime,
                                    List<DummyZVar> dummyZList, List<DummyUtilization> dummyUtilizationList) {


        for (DummyZVar dummyZVar : dummyZList) {

            z.get(dummyZVar.getA())
                    .get(dummyZVar.getT())
                    .get(dummyZVar.getI())
                    .get(dummyZVar.getJ())
                    .replace(dummyZVar.getK(),
                            z.get(dummyZVar.getA())
                                    .get(dummyZVar.getT())
                                    .get(dummyZVar.getI())
                                    .get(dummyZVar.getJ())
                                    .get(dummyZVar.getK()) - 1); // 1 Azalt
        }

        for (DummyUtilization dummyUtilization : dummyUtilizationList) {
            int d_a = dummyUtilization.getA();
            int d_t = dummyUtilization.getT();
            int d_size = dummyUtilization.getSize();

            // Size kadar azalt
            utilizationOfMachineAtTime.get(d_a).replace(d_t,
                    utilizationOfMachineAtTime.get(d_a).get(d_t) - d_size);
        }
    }

    public Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>> initializeZVar(TestCase tc) {
        Map<Integer, //a
                Map<Integer, //t
                        Map<Integer, //i
                                Map<Integer, //j
                                        Map<Integer, // k
                                                Integer>>>>> z = new HashMap<>(tc.getPhysicalMachines().size() + 1);

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
                            z.get(a).get(t).get(i).get(j).put(k, 0);
                        }
                    }
                }
            }
        }

        return z;
    }

    public List<Integer> orderBids(TestCase tc, String order) {
        if (order.contains("O2-S")) {
            return orderByRandom(tc);
        }

        if ("O1".equals(order)) { // Use Linear Relaxation
            return orderByLinearRelaxation(tc);
        } else if ("O3".equals(order)) {
            return orderByAvgProfit(tc);
        } else if ("O4".equals(order)) {
            return orderByAvgProfitEnergy(tc);
        } else if ("DEFAULT".equals(order)) {
            return IntStream.rangeClosed(1, tc.getBids().size()).boxed().collect(Collectors.toList());
        } else {
            throw new RuntimeException("Order is not defined!");
        }
    }

    private List<Integer> orderByLinearRelaxation(TestCase tc) {

        Map<String, List<Integer>> omega = tc.getOmega();

        TestSolution ts = new TestSolution();
        ts.setFeasible(true);

        Instant start = Instant.now();
        List<Integer> bidIndexes = new ArrayList<>();
        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", tc.getTestCaseName());
            env.start();

            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, tc.getTestCaseName());

            // CREATE DECISION VARIABLES
            GRBVar[] x = model.addVars(tc.getBids().size() + 1, GRB.CONTINUOUS);

            for (int i = 1; i <= tc.getBids().size(); i++) {
                x[i].set(GRB.StringAttr.VarName, "x[" + i + "]");
                x[i].set(GRB.DoubleAttr.LB, 0.00);
                x[i].set(GRB.DoubleAttr.UB, 1.00);
            }

            GRBVar[][] y = new GRBVar[tc.getBids().size() + 1][tc.getPeriod() + 1];
            for (int i = 1; i <= tc.getBids().size(); i++) {
                y[i] = model.addVars(tc.getPeriod() + 1, GRB.CONTINUOUS);

                for (int t = 1; t <= tc.getPeriod(); t++) {
                    y[i][t].set(GRB.StringAttr.VarName, "y[i=" + i + "][t=" + t + "]");

                    y[i][t].set(GRB.DoubleAttr.LB, 0.00);
                    y[i][t].set(GRB.DoubleAttr.UB, 1.00);
                }
            }

            GRBVar[][] o = new GRBVar[tc.getPhysicalMachines().size() + 1][tc.getPeriod() + 1];
            for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
                o[a] = model.addVars(tc.getPeriod() + 1, GRB.CONTINUOUS);

                for (int t = 1; t <= tc.getPeriod(); t++) {
                    o[a][t].set(GRB.StringAttr.VarName, "o[a=" + a + "][t=" + t + "]");

                    o[a][t].set(GRB.DoubleAttr.LB, 0.00);
                    o[a][t].set(GRB.DoubleAttr.UB, 1.00);
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

                                z.get(a).get(t).get(i).get(j).put(k, model.addVar(0.0, Double.POSITIVE_INFINITY, 1.0, GRB.CONTINUOUS, st));
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
            System.out.println("Time Elapsed for Relaxed Simplex Solution in seconds:" + timeElapsed);

            double obj = model.get(GRB.DoubleAttr.ObjVal);
            if (obj < 0.00001) {
                throw new RuntimeException("LINEAR RELAXATION CAN'T FIND A FEASIBLE SOLUTON");
            }

            Map<Double, List<Integer>> orderMap = new TreeMap<>(Comparator.reverseOrder());
            for (int i = 1; i <= tc.getBids().size(); i++) {
                GRBVar var = model.getVarByName("x[" + i + "]");
                Double varVal = var.get(GRB.DoubleAttr.X);

                if (orderMap.containsKey(varVal)) {
                    orderMap.get(varVal).add(i);
                } else {
                    List<Integer> dummyList = new ArrayList<>();
                    dummyList.add(i);
                    orderMap.put(varVal, dummyList);
                }
            }

            bidIndexes = orderMap.values().stream()
                    .flatMap(idx -> idx.stream())
                    .collect(Collectors.toList());

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
            ts.getErrors().add(e.getMessage() + ", Error Code:" + e.getErrorCode());
        }

        return bidIndexes;
    }

    private List<Integer> orderByRandom(TestCase tc) {
        List<Integer> list = IntStream.rangeClosed(1, tc.getBids().size()).boxed().collect(Collectors.toList());
        Collections.shuffle(list, randomG);

        return list;
    }

    private List<Integer> orderByAvgProfit(TestCase tc) {
        Map<String, List<Integer>> omega = tc.getOmega();

        Map<Double, List<Integer>> orderMap = new TreeMap<>(Comparator.reverseOrder());

        int i = 1;
        for (Bid bi : tc.getBids()) {
            int j = 1;

            double sumOfSubbidPrices = 0.00;
            for (Subbid sij : bi.getSubbidList()) {
                SubbidCtx firstAlternative = sij.getVmAlternatives().get(0);

                Integer anyPmIdx = omega.get(firstAlternative.getVmType()).get(0);
                PhysicalMachine anyPm = tc.getPhysicalMachines().get(anyPmIdx - 1);

                double p_res = anyPm.getConfiguredForVmType().getReservationPrice();

                double subbidPrice = p_res * sij.getQuantity();

                sumOfSubbidPrices += subbidPrice;

                j++;
            }

            double unitTimeRevenue = bi.getPrice().doubleValue() / bi.getDuration();

            double heurVal = unitTimeRevenue - sumOfSubbidPrices;

            if (orderMap.containsKey(heurVal)) {
                orderMap.get(heurVal).add(i);
            } else {
                List<Integer> dummyList = new ArrayList<>();
                dummyList.add(i);
                orderMap.put(heurVal, dummyList);
            }

            i++;
        }


        List<Integer> bidIndexes = orderMap.values().stream()
                .flatMap(idx -> idx.stream())
                .collect(Collectors.toList());

        return bidIndexes;
    }

    private List<Integer> orderByAvgProfitEnergy(TestCase tc) {
        Map<String, List<Integer>> omega = tc.getOmega();

        Map<Double, List<Integer>> orderMap = new TreeMap<>(Comparator.reverseOrder());

        int i = 1;
        for (Bid bi : tc.getBids()) {
            int j = 1;

            double totalPriceOfSubbids = 0.00;
            for (Subbid sij : bi.getSubbidList()) {
                SubbidCtx firstAlternative = sij.getVmAlternatives().get(0);

                Integer anyPmIdx = omega.get(firstAlternative.getVmType()).get(0);
                PhysicalMachine anyPm = tc.getPhysicalMachines().get(anyPmIdx - 1);

                double p_res = anyPm.getConfiguredForVmType().getReservationPrice();

                double reservationCost = p_res * sij.getQuantity() * firstAlternative.getVmSize();

                // CALCULATING THE ENERGY COST
                PhysicalServerConfiguration psc = physicalServerConfigManager.thetaFunction(firstAlternative.getVmType());

                int uaQuantity = physicalServerConfigManager.getuAQuantityMap().get(firstAlternative.getVmType()).get(firstAlternative.getVmSize());

                double requiredNumOfPms = Math.ceil(sij.getQuantity() / ((double) uaQuantity));
                double totalIdleCost = requiredNumOfPms * psc.getIdleCost();

                int remainingQuantity = sij.getQuantity();
                double runningMachineCost = 0.0;
                for (int ppm = 1; ppm <= requiredNumOfPms; ppm++) {

                    if (remainingQuantity < uaQuantity) {
                        runningMachineCost += ((psc.getFullLoadCost() - psc.getIdleCost()) / psc.getUA()) * (remainingQuantity * firstAlternative.getVmSize());
                    } else {
                        runningMachineCost += ((psc.getFullLoadCost() - psc.getIdleCost()) / psc.getUA()) * (uaQuantity * firstAlternative.getVmSize());
                    }

                    remainingQuantity = remainingQuantity - uaQuantity;
                }

                // RESERVATION PRICE + IDLECOST + # OF FULL MACHINES ENERGY COST + # OF NOT FULL MACHINES ENERGY COST
                double energyCost = totalIdleCost + runningMachineCost;
                totalPriceOfSubbids = totalPriceOfSubbids + (reservationCost + energyCost);

                j++;
            }

            double unitTimeRevenue = bi.getPrice().doubleValue() / bi.getDuration();

            double heurVal = unitTimeRevenue - totalPriceOfSubbids;

            if (orderMap.containsKey(heurVal)) {
                orderMap.get(heurVal).add(i);
            } else {
                List<Integer> dummyList = new ArrayList<>();
                dummyList.add(i);
                orderMap.put(heurVal, dummyList);
            }

            i++;
        }


        List<Integer> bidIndexes = orderMap.values().stream()
                .flatMap(idx -> idx.stream())
                .collect(Collectors.toList());

        return bidIndexes;
    }
}
