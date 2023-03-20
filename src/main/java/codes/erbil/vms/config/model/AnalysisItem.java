package codes.erbil.vms.config.model;

import codes.erbil.vms.entity.TestInstance;
import lombok.Data;

@Data
public class AnalysisItem {
    // Identifiers
    private String testCaseName;

    private Integer availableVmCount;
    private Double bidDensity;
    private Double subbidCount;
    private Double requestedVmAlternative;
    private Double requestedVmQuantity;
    private Integer period;

    private String testFamilyName;
    private Long testSetNo;

    // Results
    // Gurobi
    private boolean isOptimal;
    private Double gurobiObjectiveValue;
    private Long gurobiNumOfSecondsElapsed;
    private Double gurobiBidAcceptanceRatio;
    private Double gurobiMipGap;

    // GREEDY-O1 --- Linear Relaxation
    private Double greedyO1ObjectiveValue;
    private Double greedyO1ClosenessToGurobi;
    private Long greedyO1NumOfSecondsElapsed;
    private Double greedyO1BidAcceptanceRatio;

    // GREEDY-O2 --- Random Order
    private Double greedyO2ObjectiveValue;
    private Double greedyO2ClosenessToGurobi;
    private Long greedyO2NumOfSecondsElapsed;
    private Double greedyO2BidAcceptanceRatio;

    // GREEDY-O3 --- Avg. Profit
    private Double greedyO3ObjectiveValue;
    private Double greedyO3ClosenessToGurobi;
    private Long greedyO3NumOfSecondsElapsed;
    private Double greedyO3BidAcceptanceRatio;

    // GREEDY-O4 --- Avg. Profit w Energy Cost
    private Double greedyO4ObjectiveValue;
    private Double greedyO4ClosenessToGurobi;
    private Long greedyO4NumOfSecondsElapsed;
    private Double greedyO4BidAcceptanceRatio;

    // SBP-O3 --- Avg. Profit
    private Double sbpO3ObjectiveValue;
    private Double sbpO3ClosenessToGurobi;
    private Long sbpO3NumOfSecondsElapsed;
    private Double sbpO3BidAcceptanceRatio;
    // SBP-O4 --- Avg. Profit + Energy Cost
    private Double sbpO4ObjectiveValue;
    private Double sbpO4ClosenessToGurobi;
    private Long sbpO4NumOfSecondsElapsed;
    private Double sbpO4BidAcceptanceRatio;

    // MBP-O3-B10 --- Avg. Profit
    private Double mbpO3B10ObjectiveValue;
    private Double mbpO3B10ClosenessToGurobi;
    private Long mbpO3B10NumOfSecondsElapsed;
    private Double mbpO3B10BidAcceptanceRatio;

    // MBP-O3-B20 --- Avg. Profit
    private Double mbpO3B20ObjectiveValue;
    private Double mbpO3B20ClosenessToGurobi;
    private Long mbpO3B20NumOfSecondsElapsed;
    private Double mbpO3B20BidAcceptanceRatio;

    // MBP-O3-B30 --- Avg. Profit
    private Double mbpO3B30ObjectiveValue;
    private Double mbpO3B30ClosenessToGurobi;
    private Long mbpO3B30NumOfSecondsElapsed;
    private Double mbpO3B30BidAcceptanceRatio;

    // MBP-O4-B10 --- Avg. Profit
    private Double mbpO4B10ObjectiveValue;
    private Double mbpO4B10ClosenessToGurobi;
    private Long mbpO4B10NumOfSecondsElapsed;
    private Double mbpO4B10BidAcceptanceRatio;

    // MBP-O4-B20 --- Avg. Profit
    private Double mbpO4B20ObjectiveValue;
    private Double mbpO4B20ClosenessToGurobi;
    private Long mbpO4B20NumOfSecondsElapsed;
    private Double mbpO4B20BidAcceptanceRatio;

    // MBP-O3-B30 --- Avg. Profit
    private Double mbpO4B30ObjectiveValue;
    private Double mbpO4B30ClosenessToGurobi;
    private Long mbpO4B30NumOfSecondsElapsed;
    private Double mbpO4B30BidAcceptanceRatio;

    // GENETIC-P10
    private Double geneticP10ObjectiveValue;
    private Double geneticP10ClosenessToGurobi;
    private Long geneticP10NumOfSecondsElapsed;
    private Double geneticP10BidAcceptanceRatio;

    // GENETIC-P20
    private Double geneticP20ObjectiveValue;
    private Double geneticP20ClosenessToGurobi;
    private Long geneticP20NumOfSecondsElapsed;
    private Double geneticP20BidAcceptanceRatio;

    public static AnalysisItem fromTestInstance(TestInstance ti) {
        AnalysisItem ai = new AnalysisItem();

        ai.setTestCaseName(ti.testCaseName);
        ai.setBidDensity(ti.testCase.getBidDensity());
        ai.setAvailableVmCount(ti.testCase.getAvailableVmCount());
        ai.setPeriod(ti.testCase.getPeriod());
        ai.setSubbidCount(ti.testCase.getSubbidCount());
        ai.setRequestedVmAlternative(ti.testCase.getRequestedVmAlternative());
        ai.setRequestedVmQuantity(ti.testCase.getRequestedVmQuantity());

        ai.setTestFamilyName(ti.testFamilyName);
        ai.setTestSetNo(ti.testSetNo);

        return ai;
    }
}
