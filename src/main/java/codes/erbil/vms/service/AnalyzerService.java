package codes.erbil.vms.service;

import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.config.model.*;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import io.quarkus.panache.common.Parameters;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnalyzerService {

    @Inject
    VerifierManager verifierManager;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    ThreadContext threadContext;

    private List<String> headers = Arrays.asList("Test_Family_Name", "Test_Set_No", "Test_Case_Name", "Available_Vm_Count", "Period", "Bid_Density",
            "Subbid_Count", "Requested_Vm_Alternative", "Requested_Vm_Quantity",
            "Is_Optimal", "Gurobi_Objective_Value", "Gurobi_Num_Seconds", "Gurobi_Bid_Acceptance_Ratio", "MIP_GAP",
            /* "G_O1_Objective_Value, G_O1_Closeness_To_Gurobi", "G_O1_Num_of_Seconds_Elapsed", "G_O1_Bid_Acceptance_Ratio", */
            /*   "G_O2_Objective_Value, G_O2_Closeness_To_Gurobi", "G_O2_Num_of_Seconds_Elapsed", "G_O2_Bid_Acceptance_Ratio", */
            "G_O3_Objective_Value, G_O3_Closeness_To_Gurobi", "G_O3_Num_of_Seconds_Elapsed", "G_O3_Bid_Acceptance_Ratio",
            "G_O4_Objective_Value, G_O4_Closeness_To_Gurobi", "G_O4_Num_of_Seconds_Elapsed", "G_O4_Bid_Acceptance_Ratio",
            "SBP_O3_Objective_Value, SBP_O3_Closeness_To_Gurobi", "SBP_O3_Num_of_Seconds_Elapsed", "SBP_O3_Bid_Acceptance_Ratio",
            "SBP_O4_Objective_Value, SBP_O4_Closeness_To_Gurobi", "SBP_O4_Num_of_Seconds_Elapsed", "SBP_O4_Bid_Acceptance_Ratio",
            "MBP_O3_B10_Objective_Value, MBP_O3_B10_Closeness_To_Gurobi", "MBP_O3_B10_Num_of_Seconds_Elapsed", "MBP_O3_B10_Bid_Acceptance_Ratio",
            "MBP_O3_B20_Objective_Value, MBP_O3_B20_Closeness_To_Gurobi", "MBP_O3_B20_Num_of_Seconds_Elapsed", "MBP_O3_B20_Bid_Acceptance_Ratio",
            "MBP_O3_B30_Objective_Value, MBP_O3_B30_Closeness_To_Gurobi", "MBP_O3_B30_Num_of_Seconds_Elapsed", "MBP_O3_B30_Bid_Acceptance_Ratio",
            "MBP_O4_B10_Objective_Value, MBP_O4_B10_Closeness_To_Gurobi", "MBP_O4_B10_Num_of_Seconds_Elapsed", "MBP_O4_B10_Bid_Acceptance_Ratio",
            "MBP_O4_B20_Objective_Value, MBP_O4_B20_Closeness_To_Gurobi", "MBP_O4_B20_Num_of_Seconds_Elapsed", "MBP_O4_B20_Bid_Acceptance_Ratio",
            "MBP_O4_B30_Objective_Value, MBP_O4_B30_Closeness_To_Gurobi", "MBP_O4_B30_Num_of_Seconds_Elapsed", "MBP_O4_B30_Bid_Acceptance_Ratio",
            "GENETIC_P10_Objective_Value, GENETIC_P10_Closeness_To_Gurobi", "GENETIC_P10_Num_of_Seconds_Elapsed", "GENETIC_P10_Bid_Acceptance_Ratio",
            "GENETIC_P20_Objective_Value, GENETIC_P20_Closeness_To_Gurobi", "GENETIC_P20_Num_of_Seconds_Elapsed", "GENETIC_P20_Bid_Acceptance_Ratio");

    public void prepareJobStarter() {
        managedExecutor.execute(threadContext.contextualRunnable(() -> prepareAllTestFamilies()));
    }

    public void prepareJobStarter(String testFamilyName, Long testSetNo) {
        managedExecutor.execute(threadContext.contextualRunnable(
                () -> prepareSpecificTestFamilyAndSet(testFamilyName, testSetNo)));
    }

    public void prepareAllTestFamilies() {
        List<TestInstance> tiList = TestInstance.findAll().list();

        prepareResultCsv(tiList);
    }

    public void prepareSpecificTestFamilyAndSet(String testFamilyName, Long testSetNo) {
        List<TestInstance> tiList = TestInstance.find(
                "testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo)).list();

        prepareResultCsv(tiList);
    }

    public void prepareResultCsv(List<TestInstance> tiList) {

        List<AnalysisItem> analysisItems = new ArrayList<>();

        for (TestInstance ti : tiList) {

            // SET GUROBI RESULTS FIRST
            TestSolution ts = ti.solutions.stream()
                    .filter(sol -> "GUROBI".equalsIgnoreCase(sol.getSolutionStrategy()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Gurobi Çözümü olmadan analiz yapılamaz!"));

            AnalysisItem ai = AnalysisItem.fromTestInstance(ti);

            ai.setGurobiObjectiveValue(ts.getObjectiveValue());
            ai.setGurobiNumOfSecondsElapsed(ts.getTimeElapsedSeconds());
            ai.setOptimal(ts.isOptimal());
            ai.setGurobiMipGap(ts.getMipGap());

            Long numOfAcceptedBidsGurobi = getNumOfAcceptedBids(ts);

            double acceptanceRatioGurobi = numOfAcceptedBidsGurobi / ((double) ti.testCase.getBids().size());
            ai.setGurobiBidAcceptanceRatio(acceptanceRatioGurobi);

            List<TestSolution> randomSolutions = new ArrayList<>();

            for (TestSolution solution : ti.solutions) {
                if (solution.getObjectiveValue() - ts.getObjectiveValue() >= 0.01 && ts.isOptimal()) {
                    //throw new RuntimeException("GUROBI çözümü optimalken ondan büyük bir obj. value imkansızdır!");
                    System.out.println("------------------");
                    System.out.println("GUROBI çözümü optimalken ondan büyük bir obj. value bulundu!.");
                    System.out.println("TestCase Name:" + ti.testCaseName + ", Solution:" + solution.getSolutionStrategy());
                    System.out.println("GUROBI Obj Val:" + ts.getObjectiveValue() + ", Alg Obj Val:" + solution.getObjectiveValue());
                    System.out.println("------------------");
                }


                if ("GREEDY-O1".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setGreedyO1ObjectiveValue(solution.getObjectiveValue());
                    ai.setGreedyO1NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setGreedyO1BidAcceptanceRatio(acceptanceRatio);

                    ai.setGreedyO1ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("GREEDY-O3".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setGreedyO3ObjectiveValue(solution.getObjectiveValue());
                    ai.setGreedyO3NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setGreedyO3BidAcceptanceRatio(acceptanceRatio);

                    ai.setGreedyO3ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("GREEDY-O4".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setGreedyO4ObjectiveValue(solution.getObjectiveValue());
                    ai.setGreedyO4NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setGreedyO4BidAcceptanceRatio(acceptanceRatio);

                    ai.setGreedyO4ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("SBPF-O3".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setSbpO3ObjectiveValue(solution.getObjectiveValue());
                    ai.setSbpO3NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setSbpO3BidAcceptanceRatio(acceptanceRatio);

                    ai.setSbpO3ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("SBPF-O4".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setSbpO4ObjectiveValue(solution.getObjectiveValue());
                    ai.setSbpO4NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setSbpO4BidAcceptanceRatio(acceptanceRatio);

                    ai.setSbpO4ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("MBP-O3-B10".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setMbpO3B10ObjectiveValue(solution.getObjectiveValue());
                    ai.setMbpO3B10NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setMbpO3B10BidAcceptanceRatio(acceptanceRatio);

                    ai.setMbpO3B10ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("MBP-O3-B20".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setMbpO3B20ObjectiveValue(solution.getObjectiveValue());
                    ai.setMbpO3B20NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setMbpO3B20BidAcceptanceRatio(acceptanceRatio);

                    ai.setMbpO3B20ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("MBP-O3-B30".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setMbpO3B30ObjectiveValue(solution.getObjectiveValue());
                    ai.setMbpO3B30NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setMbpO3B30BidAcceptanceRatio(acceptanceRatio);

                    ai.setMbpO3B30ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("MBP-O4-B10".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setMbpO4B10ObjectiveValue(solution.getObjectiveValue());
                    ai.setMbpO4B10NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setMbpO4B10BidAcceptanceRatio(acceptanceRatio);

                    ai.setMbpO4B10ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("MBP-O4-B20".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setMbpO4B20ObjectiveValue(solution.getObjectiveValue());
                    ai.setMbpO4B20NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setMbpO4B20BidAcceptanceRatio(acceptanceRatio);

                    ai.setMbpO4B20ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("MBP-O4-B30".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setMbpO4B30ObjectiveValue(solution.getObjectiveValue());
                    ai.setMbpO4B30NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setMbpO4B30BidAcceptanceRatio(acceptanceRatio);

                    ai.setMbpO4B30ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("GENETIC-P10".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setGeneticP10ObjectiveValue(solution.getObjectiveValue());
                    ai.setGeneticP10NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setGeneticP10BidAcceptanceRatio(acceptanceRatio);

                    ai.setGeneticP10ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if ("GENETIC-P20".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    ai.setGeneticP20ObjectiveValue(solution.getObjectiveValue());
                    ai.setGeneticP20NumOfSecondsElapsed(solution.getTimeElapsedSeconds());

                    Long numOfAcceptedBids = getNumOfAcceptedBids(solution);

                    double acceptanceRatio = numOfAcceptedBids / ((double) ti.testCase.getBids().size());
                    ai.setGeneticP20BidAcceptanceRatio(acceptanceRatio);

                    ai.setGeneticP20ClosenessToGurobi(calculateClosenessToGurobi(solution.getObjectiveValue(), ai.getGurobiObjectiveValue()));

                } else if (solution.getSolutionStrategy().contains("O2-S")) { // COLLECT RANDOM SOLUTIONS
                    randomSolutions.add(solution);
                } else if ("GUROBI".equalsIgnoreCase(solution.getSolutionStrategy())) {
                    continue;
                }

            }


            //PREPARE RANDOM
            Double randomTotalObjVal = 0.0;
            Long randomTotalNumOfSecsElapsed = 0L;
            Long randomTotalNumOfAcceptedBids = 0L;

            for (TestSolution randomSol : randomSolutions) {
                randomTotalObjVal += randomSol.getObjectiveValue();
                randomTotalNumOfSecsElapsed += randomSol.getTimeElapsedSeconds();

                Long numOfAcceptedBids = getNumOfAcceptedBids(randomSol);

                randomTotalNumOfAcceptedBids += numOfAcceptedBids;
            }

            if (randomSolutions.size() > 0) {
                double avgObjVal = randomTotalObjVal / randomSolutions.size();
                ai.setGreedyO2ObjectiveValue(avgObjVal);

                ai.setGreedyO2NumOfSecondsElapsed(randomTotalNumOfSecsElapsed / randomSolutions.size());

                Double avgNumOfAcceptedBids = randomTotalNumOfAcceptedBids / (double) randomSolutions.size();

                double acceptanceRatio = avgNumOfAcceptedBids / ti.testCase.getBids().size();
                ai.setGreedyO2BidAcceptanceRatio(acceptanceRatio);

                ai.setGreedyO2ClosenessToGurobi(calculateClosenessToGurobi(avgObjVal, ai.getGurobiObjectiveValue()));
            }

            analysisItems.add(ai);
        }

        StringBuilder sb = new StringBuilder(String.join(",", headers));
        sb.append("\n");

        int numOfExcludedTestCases = 0;
        for (AnalysisItem analItem : analysisItems) {
            if (analItem.getGurobiObjectiveValue() == 0.0) {
                numOfExcludedTestCases++;
                System.out.println("Test case is excluded from the results because Gurobi can't find a solution for this test case: " + analItem.getTestCaseName());
                continue;
            }

            sb.append(analItem.getTestFamilyName() + ",");
            sb.append(analItem.getTestSetNo() + ",");
            sb.append(analItem.getTestCaseName() + ",");
            sb.append(analItem.getAvailableVmCount() + ",");
            sb.append(analItem.getPeriod() + ",");
            sb.append(analItem.getBidDensity() + ",");
            sb.append(analItem.getSubbidCount() + ",");
            sb.append(analItem.getRequestedVmAlternative() + ",");
            sb.append(analItem.getRequestedVmQuantity() + ",");
            // GUROBI
            sb.append(analItem.isOptimal() + ",");
            sb.append(analItem.getGurobiObjectiveValue() + ",");
            sb.append(analItem.getGurobiNumOfSecondsElapsed() + ",");
            sb.append(analItem.getGurobiBidAcceptanceRatio() + ",");
            sb.append(analItem.getGurobiMipGap() + ",");
            // GREEDY-O1
            /* sb.append(analItem.getGreedyO1ObjectiveValue() + ",");
            sb.append(analItem.getGreedyO1ClosenessToGurobi() + ",");
            sb.append(analItem.getGreedyO1NumOfSecondsElapsed() + ",");
            sb.append(analItem.getGreedyO1BidAcceptanceRatio() + ","); */
            // GREEDY-O2
            /* sb.append(analItem.getGreedyO2ObjectiveValue() + ",");
            sb.append(analItem.getGreedyO2ClosenessToGurobi() + ",");
            sb.append(analItem.getGreedyO2NumOfSecondsElapsed() + ",");
            sb.append(analItem.getGreedyO2BidAcceptanceRatio() + ","); */
            // GREEDY-O3
            sb.append(analItem.getGreedyO3ObjectiveValue() + ",");
            sb.append(analItem.getGreedyO3ClosenessToGurobi() + ",");
            sb.append(analItem.getGreedyO3NumOfSecondsElapsed() + ",");
            sb.append(analItem.getGreedyO3BidAcceptanceRatio() + ",");

            // GREEDY-O4
            sb.append(analItem.getGreedyO4ObjectiveValue() + ",");
            sb.append(analItem.getGreedyO4ClosenessToGurobi() + ",");
            sb.append(analItem.getGreedyO4NumOfSecondsElapsed() + ",");
            sb.append(analItem.getGreedyO4BidAcceptanceRatio() + ",");

            // SBP-O3
            sb.append(analItem.getSbpO3ObjectiveValue() + ",");
            sb.append(analItem.getSbpO3ClosenessToGurobi() + ",");
            sb.append(analItem.getSbpO3NumOfSecondsElapsed() + ",");
            sb.append(analItem.getSbpO3BidAcceptanceRatio() + ",");

            // SBP-O4
            sb.append(analItem.getSbpO4ObjectiveValue() + ",");
            sb.append(analItem.getSbpO4ClosenessToGurobi() + ",");
            sb.append(analItem.getSbpO4NumOfSecondsElapsed() + ",");
            sb.append(analItem.getSbpO4BidAcceptanceRatio() + ",");

            // MBP-O3-B10
            sb.append(analItem.getMbpO3B10ObjectiveValue() + ",");
            sb.append(analItem.getMbpO3B10ClosenessToGurobi() + ",");
            sb.append(analItem.getMbpO3B10NumOfSecondsElapsed() + ",");
            sb.append(analItem.getMbpO3B10BidAcceptanceRatio() + ",");
            // MBP-O3-B20
            sb.append(analItem.getMbpO3B20ObjectiveValue() + ",");
            sb.append(analItem.getMbpO3B20ClosenessToGurobi() + ",");
            sb.append(analItem.getMbpO3B20NumOfSecondsElapsed() + ",");
            sb.append(analItem.getMbpO3B20BidAcceptanceRatio() + ",");
            // MBP-O3-B30
            sb.append(analItem.getMbpO3B30ObjectiveValue() + ",");
            sb.append(analItem.getMbpO3B30ClosenessToGurobi() + ",");
            sb.append(analItem.getMbpO3B30NumOfSecondsElapsed() + ",");
            sb.append(analItem.getMbpO3B30BidAcceptanceRatio() + ",");

            // MBP-O4-B10
            sb.append(analItem.getMbpO4B10ObjectiveValue() + ",");
            sb.append(analItem.getMbpO4B10ClosenessToGurobi() + ",");
            sb.append(analItem.getMbpO4B10NumOfSecondsElapsed() + ",");
            sb.append(analItem.getMbpO4B10BidAcceptanceRatio() + ",");
            // MBP-O4-B20
            sb.append(analItem.getMbpO4B20ObjectiveValue() + ",");
            sb.append(analItem.getMbpO4B20ClosenessToGurobi() + ",");
            sb.append(analItem.getMbpO4B20NumOfSecondsElapsed() + ",");
            sb.append(analItem.getMbpO4B20BidAcceptanceRatio() + ",");
            // MBP-O4-B30
            sb.append(analItem.getMbpO4B30ObjectiveValue() + ",");
            sb.append(analItem.getMbpO4B30ClosenessToGurobi() + ",");
            sb.append(analItem.getMbpO4B30NumOfSecondsElapsed() + ",");
            sb.append(analItem.getMbpO4B30BidAcceptanceRatio() + ",");

            // GENETIC-P10
            sb.append(analItem.getGeneticP10ObjectiveValue() + ",");
            sb.append(analItem.getGeneticP10ClosenessToGurobi() + ",");
            sb.append(analItem.getGeneticP10NumOfSecondsElapsed() + ",");
            sb.append(analItem.getGeneticP10BidAcceptanceRatio() + ",");
            // GENETIC-P20
            sb.append(analItem.getGeneticP20ObjectiveValue() + ",");
            sb.append(analItem.getGeneticP20ClosenessToGurobi() + ",");
            sb.append(analItem.getGeneticP20NumOfSecondsElapsed() + ",");
            sb.append(analItem.getGeneticP20BidAcceptanceRatio());

            sb.append("\n");
        }

        System.out.println("# of excluded test cases in results.csv file:" + numOfExcludedTestCases);

        Path path = Paths.get("results.csv");
        byte[] strToBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        try {
            Files.write(path, strToBytes);
            System.out.println("Analiz classpath altında results.csv olarak kaydedilmiştir!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Double calculateClosenessToGurobi(Double heurObjVal, Double gurobiObjVal) {
        if (gurobiObjVal == 0) {
            return 1.0;
        } else {
            return heurObjVal / gurobiObjVal;
        }
    }

    public Long getNumOfAcceptedBids(TestSolution ts) {
        if (ts.getDecisionVariables() == null || ts.getDecisionVariables().isEmpty()) return 0L;

        return ts.getDecisionVariables().keySet().stream()
                .filter(v -> v.contains("x["))
                .count();
    }

    public void avgPhysicalMachineLoadByAvmcAndPeriod() {
        List<TestInstance> tiList = TestInstance.findAll().list();

        // AVAILABLE VM COUNT -> { m_a -> { t -> { Utilization } } }
        Map<Integer, Map<Integer, Map<Integer, Integer>>> myMap = new HashMap<>();

        Map<Integer, Integer> countMap = new HashMap<>();

        for (TestInstance ti : tiList) {
            Integer avmc = ti.testCase.getAvailableVmCount();
            for (TestSolution solution : ti.solutions) {
                System.out.println("Deneme");
            }
        }

    }

    public Map<Integer, Map<Integer, Integer>> getPhysicalMachineCapacityByTimeSlots(TestCase tc, TestSolution ts) {
        Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime = verifierManager.initializeMachineTimeMap(tc);

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            for (int t = 1; t <= tc.getPeriod(); t++) {
                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);
                    int duration = tc.getBids().get(i - 1).getDuration();

                    for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                        Subbid subbid = bid.getSubbidList().get(j - 1);

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                            if (ts.getDecisionVariables().containsKey("z[a=" + a + "][t=" + t + "][i=" + i + "][j=" + j + "][k=" + k + "]")) {
                                Double val = ts.getDecisionVariables().get("z[a=" + a + "][t=" + t + "][i=" + i + "][j=" + j + "][k=" + k + "]");

                                Integer util = vmAlternative.getVmSize() * val.intValue();

                                // Duration kadar time slotlarını dolaş ve utilization güncelle
                                for (int t_prime = t; t_prime <= t + duration - 1; t_prime++) {
                                    Integer newUtil = utilizationOfMachineAtTime.get(a).get(t_prime) + util;

                                    utilizationOfMachineAtTime.get(a).replace(t_prime, newUtil);
                                }
                            }
                        }
                    }
                }
            }
        }

        return utilizationOfMachineAtTime;
    }

    public String numOfBidsCsv() {
        List<TestInstance> tiList = TestInstance.findAll().list();

        List<AnalysisItem> analysisItems = tiList.stream()
                .map(ti -> AnalysisItem.fromTestInstance(ti))
                .collect(Collectors.toList());

        List<String> localheaders = Arrays.asList("Test_Family_Name", "Test_Set_No", "Test_Case_Name", "Available_Vm_Count", "Period", "Bid_Density",
                "Subbid_Count", "Requested_Vm_Alternative", "Requested_Vm_Quantity", "Number_of_Bids");

        StringBuilder sb = new StringBuilder(String.join(",", localheaders));
        sb.append("\n");

        for (TestInstance ti : tiList) {
            sb.append(ti.testFamilyName + ",");
            sb.append(ti.testSetNo + ",");
            sb.append(ti.testCase.getTestCaseName() + ",");
            sb.append(ti.testCase.getAvailableVmCount() + ",");
            sb.append(ti.testCase.getPeriod() + ",");
            sb.append(ti.testCase.getBidDensity() + ",");
            sb.append(ti.testCase.getSubbidCount() + ",");
            sb.append(ti.testCase.getRequestedVmAlternative() + ",");
            sb.append(ti.testCase.getRequestedVmQuantity() + ",");

            sb.append(ti.testCase.getBids().size());

            sb.append("\n");
        }

        return sb.toString();
    }


}
