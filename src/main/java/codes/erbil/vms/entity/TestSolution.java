package codes.erbil.vms.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class TestSolution {
    private String solutionStrategy;
    private Map<String, Double> decisionVariables;
    private Double objectiveValue;
    private Long timeElapsedSeconds;
    private boolean optimal;
    private boolean feasible;
    private List<String> errors = new ArrayList<>();

    double mipGap;
}
