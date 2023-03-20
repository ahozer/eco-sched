package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.workflow.WorkflowGeneticSolver;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import java.util.Optional;

@QuarkusTest
public class WorkflowGeneticSolverTest {

    @Inject
    WorkflowGeneticSolver workflowGeneticSolver;

    @Inject
    VerifierManager verifierManager;

    //@Test
    void solve_from_db() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 3)
                                .and("testCaseName", "AVMC1024_T5_BD0.5_SBC1.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = workflowGeneticSolver.solve(ti.testCase, "GREEDY", "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));

    }
}
