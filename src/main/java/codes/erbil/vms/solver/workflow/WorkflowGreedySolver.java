package codes.erbil.vms.solver.workflow;

import codes.erbil.vms.config.manager.PhysicalServerConfigManager;
import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.config.model.*;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.GreedySolver;
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
public class WorkflowGreedySolver {

    @Inject
    Random randomG;

    @Inject
    VerifierManager verifierManager;

    @Inject
    PhysicalServerConfigManager physicalServerConfigManager;

    @Inject
    GreedySolver greedySolver;

    @ConfigProperty(name = "vms.solver.greedy.relaxation.timelimit")
    Long timeLimit;

    @ConfigProperty(name = "vms.solver.greedy.thread.limit", defaultValue = "1")
    int singleCaseThreadLimit;

    public TestSolution solve(TestCase tc, List<Integer> orderedWorkflowIndexes, String order) {
        Instant start = Instant.now();

        // Physical machine a -> { Time Slot t -> {Utilization Level} }
        Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime = verifierManager.initializeMachineTimeMap(tc);

        // z[a][t][i][j][k]
        Map<Integer, //a
                Map<Integer, //t
                        Map<Integer, //i
                                Map<Integer, //j
                                        Map<Integer, // k
                                                Integer>>>>> z = greedySolver.initializeZVar(tc);

        Map<String, List<Integer>> omega = tc.getOmega();

        TestSolution ts = new TestSolution();
        ts.setFeasible(true);

        Double currentObjVal = 0.00;

        for (Integer l : orderedWorkflowIndexes) {
            Workflow workflow = tc.getWorkflowList().get(l - 1);

            List<DummyZVar> dummyZForBids = new ArrayList<>();
            List<DummyUtilization> dummyUtilForBids = new ArrayList<>();
            boolean workflowAccepted = true;

            Map<Integer, Integer> bidToStartTime = new HashMap<>();

            int bidCtr = 0;
            for (Integer i : workflow.getBids()) {
                Bid bid = tc.getBids().get(i - 1);

                int calculatedEarliestTime = bid.getEarliestTime();
                if (bid.getDependencyList() != null && bid.getDependencyList().size() > 0) {
                    for (Integer dependencyBidIndex : bid.getDependencyList()) {
                        if (bidToStartTime.containsKey(dependencyBidIndex)) {
                            int depDuration = tc.getBids().get(dependencyBidIndex - 1).getDuration();
                            calculatedEarliestTime = Math.max((bidToStartTime.get(dependencyBidIndex) + depDuration), calculatedEarliestTime);
                        } else {
                            workflowAccepted = false;
                            break;
                        }
                    }
                }

                if (!workflowAccepted) {
                    break;
                }
                ;

                boolean bidAccepted = false; // Bütün subbidler yerleştirilse bile obj. value değeri azalırsa o bid kabul edilmez.
                boolean allSubbidsArePlaced = false;
                for (int t = calculatedEarliestTime; t <= bid.getLatestTime() - bid.getDuration() + 1; t++) {

                    List<DummyZVar> dummyZForSubbids = new ArrayList<>();
                    List<DummyUtilization> dummyUtilForSubbids = new ArrayList<>();

                    boolean currentSubbidsArePlaced = true;
                    int j = 1;
                    for (Subbid sb : bid.getSubbidList()) {


                        int remainingQty = sb.getQuantity();
                        boolean subbidPlaced = false;

                        List<DummyZVar> dummyZForVmAlternatives = new ArrayList<>();
                        List<DummyUtilization> dummyUtilForVmAlternatives = new ArrayList<>();


                        // Bütün VM alternatifleri gezilir (ilk sırada zaten en ucuz res. price'a sahip olan gelecek)
                        int k = 1;
                        for (SubbidCtx sbc : sb.getVmAlternatives()) {

                            // VM'i çalıştırmaya yapılandırılmış fiziksel sunucular gezilir.
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
                                    // t den başlayıp duration kadar ilerleyeceksin.
                                    // Her bir time slotta yeterli yer bulunması gerekiyor.
                                    for (int t_prime = t; t_prime <= t + bid.getDuration() - 1; t_prime++) {
                                        // Eklenebilir yer yok
                                        if (sbc.getVmSize() > pm.getUA() - utilizationOfMachineAtTime.get(a).get(t_prime)) {
                                            vmAlternativePlaced = false;
                                            break;
                                        }
                                    }

                                    // Yerlestirilebilirse ilgili degiskenleri guncelle
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

                            greedySolver.degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForVmAlternatives, dummyUtilForVmAlternatives);

                            // 1 Subbidin yerleştirilememesi bile
                            // bid'i satisfy etmediği için geri kalan subbidlere bakmaya gerek kalmaz.
                            // Bir sonraki t değeri için denenir

                            currentSubbidsArePlaced = false;
                        }

                        if (!currentSubbidsArePlaced) {
                            // Önceki subbidlerde yerleştirilenleri geri alır
                            greedySolver.degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForSubbids, dummyUtilForSubbids);

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
                        TestSolution dummySolution = new TestSolution();
                        dummySolution.setSolutionStrategy("DUMMY_GREEDY");
                        Map<String, Double> dummyDecVar = prepareDecisionVariables(z, tc);
                        dummySolution.setDecisionVariables(dummyDecVar);

                        Double calculatedObjVal = verifierManager.getObjValue(tc, dummySolution);

                        // Bid kabul edilir
                        if (calculatedObjVal > currentObjVal) {
                            dummyZForBids.addAll(dummyZForSubbids);
                            dummyUtilForBids.addAll(dummyUtilForSubbids);

                            currentObjVal = calculatedObjVal;
                            bidAccepted = true;

                            bidToStartTime.put(i, t);
                        } else {
                            // Kabul edilmez geri alınır.
                            greedySolver.degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForSubbids, dummyUtilForSubbids);
                        }

                    }

                    if (bidAccepted) {
                        break;
                    }
                }
            }

            if (workflowAccepted && bidToStartTime.size() == workflow.getBids().size()) {
                TestSolution dummySolution = new TestSolution();
                dummySolution.setSolutionStrategy("DUMMY_GREEDY");
                Map<String, Double> dummyDecVar = prepareDecisionVariables(z, tc);
                dummySolution.setDecisionVariables(dummyDecVar);

                dummyDecVar.put("w[" + l + "]", 1.0);

                currentObjVal = verifierManager.getObjValue(tc, dummySolution);
                ts.setDecisionVariables(dummyDecVar);
            } else {
                greedySolver.degiskenleriGeriAl(z, utilizationOfMachineAtTime, dummyZForBids, dummyUtilForBids);

                TestSolution dummySolution = new TestSolution();
                dummySolution.setSolutionStrategy("DUMMY_GREEDY");
                Map<String, Double> dummyDecVar = prepareDecisionVariables(z, tc);
                dummySolution.setDecisionVariables(dummyDecVar);

                currentObjVal = verifierManager.getObjValue(tc, dummySolution);

                dummyZForBids = new ArrayList<>();
                dummyUtilForBids = new ArrayList<>();
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

        for (int l = 1; l <= tc.getWorkflowList().size(); l++) {
            Workflow workflow = tc.getWorkflowList().get(l - 1);

            boolean includeWorkflow = true;
            for (Integer i : workflow.getBids()) {
                Bid bid = tc.getBids().get(i - 1);

                if (!decisionVariables.containsKey("x[" + i + "]")) {
                    includeWorkflow = false;
                    break;
                }
            }

            if (includeWorkflow) {
                decisionVariables.put("w[" + l + "]", Double.valueOf(1.0));
            }

        }

        return decisionVariables;
    }

    public List<Integer> orderWorkflows(TestCase tc, String order) {
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

    private List<Integer> orderByRandom(TestCase tc) {
        List<Integer> list = IntStream.rangeClosed(1, tc.getWorkflowList().size()).boxed().collect(Collectors.toList());
        Collections.shuffle(list, randomG);

        return list;
    }

    private List<Integer> orderByLinearRelaxation(TestCase tc) {
        Map<String, List<Integer>> omega = tc.getOmega();

        TestSolution ts = new TestSolution();
        ts.setFeasible(true);

        List<Integer> workflowIndexes = new ArrayList<>();
        Instant start = Instant.now();
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

            GRBVar[] w = model.addVars(tc.getWorkflowList().size() + 1, GRB.CONTINUOUS);

            for (int l = 1; l <= tc.getWorkflowList().size(); l++) {
                w[l].set(GRB.StringAttr.VarName, "w[" + l + "]");
                w[l].set(GRB.DoubleAttr.LB, 0.00);
                w[l].set(GRB.DoubleAttr.UB, 1.00);
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

            for (int l = 1; l <= tc.getWorkflowList().size(); l++) {
                obj1.addTerm(tc.getWorkflowList().get(l - 1).getPrice().doubleValue(), w[l]);
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

            obj2.add(obj3); // add cost
            obj2.add(obj4); // add cost
            obj1.multAdd(-1, obj2); // remove cost from the revenue

            model.setObjective(obj1, GRB.MAXIMIZE);

            // Eq #8
            for (int i = 1; i <= tc.getBids().size(); i++) {
                Bid bid = tc.getBids().get(i - 1);
                int sumEnd = bid.getLatestTime() - bid.getDuration() + 1;

                GRBLinExpr eq8 = new GRBLinExpr();

                for (int t = bid.getEarliestTime(); t <= sumEnd; t++) {
                    eq8.addTerm(1, y[i][t]);
                }

                eq8.addTerm(-1, x[i]);
                model.addConstr(eq8, GRB.EQUAL, 0, "Eq8[i=" + i + "]");
            }

            // Eq #9
            for (int i = 1; i <= tc.getBids().size(); i++) {
                Bid bid = tc.getBids().get(i - 1);
                int earliest = tc.getBids().get(i - 1).getEarliestTime();
                int latest = tc.getBids().get(i - 1).getLatestTime();
                int duration = tc.getBids().get(i - 1).getDuration();

                for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                    Subbid subbid = bid.getSubbidList().get(j - 1);

                    for (int t = earliest; t <= latest - duration + 1; t++) {
                        GRBLinExpr eq9 = new GRBLinExpr();

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                            // If that VM Type has not mapped by any Physical machine
                            if (!omega.containsKey(vmAlternative.getVmType())) {
                                continue;
                            }

                            for (Integer a : omega.get(vmAlternative.getVmType())) {
                                eq9.addTerm(1, z.get(a).get(t).get(i).get(j).get(k));
                            }
                        }


                        eq9.addTerm(-1 * subbid.getQuantity(), y[i][t]);

                        String st = "Eq9[i=" + i + "][j=" + j + "][t=" + t + "]";
                        model.addConstr(eq9, GRB.EQUAL, 0, st);
                    }
                }
            }

            // Eq #10
            for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
                PhysicalMachine pm = tc.getPhysicalMachines().get(a - 1);

                for (int t = 1; t <= tc.getPeriod(); t++) {
                    GRBLinExpr eq10Lhs = new GRBLinExpr();
                    GRBLinExpr eq10Rhs = new GRBLinExpr();

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
                                    eq10Lhs.addTerm(vmAlternative.getVmSize(), z.get(a).get(t_prime).get(i).get(j).get(k));
                                }
                            }
                        }
                    }

                    eq10Rhs.addTerm(pm.getUA(), o[a][t]);
                    String st = "Eq10[a=" + a + "][t=" + t + "]";
                    model.addConstr(eq10Lhs, GRB.LESS_EQUAL, eq10Rhs, st);
                }
            }

            // ### YENI ### EQ(11) - WORKFLOW'A AIT BIDLERIN WORKFLOWA ESITLENMESI
            for (int l = 1; l <= tc.getWorkflowList().size(); l++) {
                Workflow workflow = tc.getWorkflowList().get(l - 1);

                for (Integer bidIndex : workflow.getBids()) {
                    String st = "Eq11[l=" + l + "][i=" + bidIndex + "]";

                    model.addConstr(x[bidIndex], GRB.EQUAL, w[l], st);
                }
            }

            // ### YENI ### EQ(12) - DEPENDENCY'LERDEN ÖNCE BAŞLAMAMASINI SAGLAYAN CONSTR
            for (int l = 1; l <= tc.getWorkflowList().size(); l++) {
                Workflow workflow = tc.getWorkflowList().get(l - 1);

                for (Integer bidIndex : workflow.getBids()) {
                    Bid bid = tc.getBids().get(bidIndex - 1);
                    int e_i = bid.getEarliestTime();
                    int l_i = bid.getLatestTime();
                    int d_i = bid.getDuration();

                    // DEPENDENCY YOKSA WORKFLOW'DAKI DIGER BIDLER ICIN DENENIR
                    if (bid.getDependencyList() == null || bid.getDependencyList().size() == 0) continue;

                    for (Integer depenBidIndex : bid.getDependencyList()) {
                        Bid dependencyBid = tc.getBids().get(depenBidIndex - 1);
                        int e_i_prime = dependencyBid.getEarliestTime();
                        int l_i_prime = dependencyBid.getLatestTime();
                        int d_i_prime = dependencyBid.getDuration();


                        for (int t = e_i; t <= l_i - d_i + 1; t++) {
                            // IC DONGULER BASLANGIC
                            GRBLinExpr eq12_ytoplam = new GRBLinExpr();

                            for (int t_prime = Math.max(e_i_prime, t - d_i_prime + 1); t_prime <= (l_i_prime - d_i_prime + 1); t_prime++) {
                                eq12_ytoplam.addTerm(1, y[depenBidIndex.intValue()][t_prime]);
                            }

                            GRBLinExpr eq12_rhs = new GRBLinExpr();

                            eq12_rhs.addConstant(1);
                            eq12_rhs.addTerm(-1, y[bidIndex][t]);


                            String st = "Eq12[l=" + l + "][i=" + bidIndex + "][i_prime=" + depenBidIndex + "]";
                            model.addConstr(eq12_ytoplam, GRB.LESS_EQUAL, eq12_rhs, st);
                        }
                    }
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
                //throw new RuntimeException("LINEAR RELAXATION CAN'T FIND A FEASIBLE SOLUTON");
                return orderByRandom(tc);
            }

            Map<Integer, Double> bidToHeurMap = new HashMap<>();
            for (int i = 1; i <= tc.getBids().size(); i++) {
                GRBVar var = model.getVarByName("x[" + i + "]");
                Double varVal = var.get(GRB.DoubleAttr.X);

                bidToHeurMap.put(i, varVal);
            }

            workflowIndexes = getWorkflowIndexes(tc, bidToHeurMap);


        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
            ts.getErrors().add(e.getMessage() + ", Error Code:" + e.getErrorCode());
        }

        return workflowIndexes;
    }

    private List<Integer> orderByAvgProfit(TestCase tc) {

        Map<String, List<Integer>> omega = tc.getOmega();

        Map<Integer, Double> bidToHeurMap = new HashMap<>();

        int i = 1;
        for (Bid bi : tc.getBids()) {
            double sumOfSubbidPrices = 0.00;
            for (Subbid sij : bi.getSubbidList()) {
                SubbidCtx firstAlternative = sij.getVmAlternatives().get(0);

                Integer anyPmIdx = omega.get(firstAlternative.getVmType()).get(0);
                PhysicalMachine anyPm = tc.getPhysicalMachines().get(anyPmIdx - 1);

                double p_res = anyPm.getConfiguredForVmType().getReservationPrice();

                double subbidPrice = p_res * sij.getQuantity();

                sumOfSubbidPrices += subbidPrice;
            }

            double unitTimeRevenue = bi.getPrice().doubleValue() / bi.getDuration();

            double heurVal = unitTimeRevenue - sumOfSubbidPrices;

            bidToHeurMap.put(i, heurVal);

            i++;
        }

        List<Integer> workflowIndexes = getWorkflowIndexes(tc, bidToHeurMap);

        return workflowIndexes;
    }

    private List<Integer> orderByAvgProfitEnergy(TestCase tc) {
        Map<String, List<Integer>> omega = tc.getOmega();

        Map<Integer, Double> bidToHeurMap = new HashMap<>();

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

                // ENERGY COST HESAPLAMA
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


            bidToHeurMap.put(i, heurVal);

            i++;
        }

        List<Integer> workflowIndexes = getWorkflowIndexes(tc, bidToHeurMap);

        return workflowIndexes;
    }

    private List<Integer> getWorkflowIndexes(TestCase tc, Map<Integer, Double> bidToHeurMap) {

        Map<Double, List<Integer>> orderMap = new TreeMap<>(Comparator.reverseOrder());
        for (int l = 1; l <= tc.getWorkflowList().size(); l++) {
            Workflow workflow = tc.getWorkflowList().get(l - 1);

            double totalHeurInWorkflow = 0.0;
            for (Integer bidIndex : workflow.getBids()) {
                totalHeurInWorkflow += bidToHeurMap.get(bidIndex).doubleValue();
            }

            double avgHeurInWorkflow = totalHeurInWorkflow / workflow.getBids().size();

            if (orderMap.containsKey(avgHeurInWorkflow)) {
                orderMap.get(avgHeurInWorkflow).add(l);
            } else {
                List<Integer> dummyList = new ArrayList<>();
                dummyList.add(l);
                orderMap.put(avgHeurInWorkflow, dummyList);
            }
        }

        List<Integer> workflowIndexes = orderMap.values().stream()
                .flatMap(idx -> idx.stream())
                .collect(Collectors.toList());
        return workflowIndexes;
    }
}
