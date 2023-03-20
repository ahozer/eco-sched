package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.workflow.WorkflowGreedySolver;
import codes.erbil.vms.solver.workflow.WorkflowMultipleBidPlacementSolver;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@QuarkusTest
public class WorkflowMbpSolverTest {

    @Inject
    WorkflowMultipleBidPlacementSolver workflowMultipleBidPlacementSolver;

    @Inject
    WorkflowGreedySolver workflowGreedySolver;

    @Inject
    VerifierManager verifierManager;

    //@Test
    void workflowMbp_solve_from_db() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 3)
                                .and("testCaseName", "AVMC1024_T5_BD0.5_SBC1.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedWorkflowIndexes = workflowGreedySolver.orderWorkflows(ti.testCase, "O3");

        TestSolution ts = workflowMultipleBidPlacementSolver.solve(ti.testCase, orderedWorkflowIndexes, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void workflowMbp_solve_from_db2() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 3)
                                .and("testCaseName", "AVMC3072_T5_BD0.5_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedWorkflowIndexes = workflowGreedySolver.orderWorkflows(ti.testCase, "O4");

        TestSolution ts = workflowMultipleBidPlacementSolver.solve(ti.testCase, orderedWorkflowIndexes, "O4");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

}
