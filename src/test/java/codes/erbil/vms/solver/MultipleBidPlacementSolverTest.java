package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@QuarkusTest
public class MultipleBidPlacementSolverTest {

    @Inject
    MultipleBidPlacementSolver multipleBidPlacementSolver;

    @Inject
    GreedySolver greedySolver;

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O3");

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase4_avgProfitOrder() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD5.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase5_avgProfitOrder() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T15_BD3.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O3");

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }


    // ENERGY
    //@Test
    void solve_realCase2_avgProfitOrderEnergy() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC1024_T10_BD1.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase3_avgProfitOrderEnergy() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T10_BD2.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase6_avgProfitOrderEnergy() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC2048_T10_BD2.0_SBC3.0_A3.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc1() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD0.25_SBC2.0_A2.0_Q1.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc2() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T15_BD0.5_SBC1.0_A2.0_Q1.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc3() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T15_BD0.5_SBC2.0_A2.0_Q1.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc4() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T5_BD3.0_SBC1.0_A1.0_Q1.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc5() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC2048_T20_BD0.5_SBC2.0_A3.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc6() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T5_BD5.0_SBC1.0_A2.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc7() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T5_BD5.0_SBC3.0_A2.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc8() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC2048_T10_BD4.0_SBC1.0_A1.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc9() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T5_BD4.0_SBC3.0_A3.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_zyfix_tc10() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T20_BD0.25_SBC2.0_A3.0_Q2.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O4");
        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }
}
