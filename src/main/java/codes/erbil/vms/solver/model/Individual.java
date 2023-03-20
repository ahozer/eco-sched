package codes.erbil.vms.solver.model;

import codes.erbil.vms.entity.TestSolution;

import java.util.List;

public class Individual {
    private List<Integer> orderedBidIndexes;
    private TestSolution testSolution;

    public Individual() {
    }

    public Individual(List<Integer> orderedBidIndexes, TestSolution testSolution) {
        this.orderedBidIndexes = orderedBidIndexes;
        this.testSolution = testSolution;
    }

    public List<Integer> getOrderedBidIndexes() {
        return orderedBidIndexes;
    }

    public TestSolution getTestSolution() {
        return testSolution;
    }

    public void setTestSolution(TestSolution testSolution) {
        this.testSolution = testSolution;
    }

    @Override
    public String toString() {
        return "Individual{" +
                "ObjectiveValue=" + testSolution.getObjectiveValue() +
                '}';
    }
}
