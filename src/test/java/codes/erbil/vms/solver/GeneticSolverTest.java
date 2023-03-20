package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import java.util.Optional;

@QuarkusTest
public class GeneticSolverTest {

    @Inject
    GeneticSolver geneticSolver;

    @Inject
    VerifierManager verifierManager;

    //@Test
    void solve_realCase2_avgProfitOrder() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC1024_T10_BD1.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase3_avgProfitOrder() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T10_BD2.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase4_avgProfitOrderEnergy() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD5.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase5_avgProfitOrderEnergy() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T15_BD3.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));
        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase6_avgProfitOrder() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC2048_T10_BD2.0_SBC3.0_A3.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc1() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T10_BD2.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc2() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T10_BD3.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc3() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T10_BD4.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc4() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T10_BD5.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc5() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD2.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc6() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD3.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc7() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD4.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_sub_tc8() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD5.0_SBC2.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());
        System.out.println("OBJ VAL:" + ts.getObjectiveValue());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }
}
