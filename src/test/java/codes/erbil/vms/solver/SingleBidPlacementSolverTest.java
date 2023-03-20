package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.PhysicalMachineManager;
import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.Subbid;
import codes.erbil.vms.config.model.SubbidCtx;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@QuarkusTest
public class SingleBidPlacementSolverTest {

    @Inject
    PhysicalMachineManager physicalMachineManager;

    @Inject
    SingleBidPlacementSolver singleBidPlacementSolver;

    @Inject
    GreedySolver greedySolver;

    @Inject
    VerifierManager verifierManager;

    //@Test
    void solve_realCase1_avgProfitOrder() {
        TestCase tc = createTestCaseForSBP_RealCase1();

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "O3");

        TestSolution ts = singleBidPlacementSolver.solve(tc, orderedBidIndexes, true, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    //@Test
    void solve_realCase2_avgProfitOrder_fixed() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC1024_T10_BD1.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase3_avgProfitOrder_fixed() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T10_BD2.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase4_avgProfitOrder_fixed() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD5.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase2_avgProfitOrder_batch() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC1024_T10_BD1.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, false, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase3_avgProfitOrder_batch() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC3072_T10_BD2.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, false, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase4_avgProfitOrder_batch() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T20_BD5.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, false, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase5_avgProfitOrder_batch() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC4096_T15_BD3.0_SBC2.0_A1.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, false, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }

    //@Test
    void solve_realCase6_avgProfitOrder_batch() {
        Optional<TestInstance> tiOpt = TestInstance.find(
                        "testFamilyName=:testFamilyName and testSetNo=:testSetNo and testCaseName=:testCaseName",
                        Parameters.with("testFamilyName", "CONFIG-4")
                                .and("testSetNo", 1)
                                .and("testCaseName", "AVMC2048_T10_BD2.0_SBC3.0_A3.0_Q3.0"))
                .firstResultOptional();

        TestInstance ti = tiOpt.orElseThrow(() -> new RuntimeException("TestInstance cannot be found"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, "O3");
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, false, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }


    private TestCase createTestCaseForSBP_RealCase1() {
        List<Bid> bidList = new ArrayList<>();

        // Bid 1
        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v6");
        sbc11_1.setVmSize(48);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1));
        sb11.setQuantity(1);

        SubbidCtx sbc12_1 = new SubbidCtx();
        sbc12_1.setVmType("v1");
        sbc12_1.setVmSize(16);

        Subbid sb12 = new Subbid();
        sb12.setVmAlternatives(Arrays.asList(sbc12_1));
        sb12.setQuantity(2);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11, sb12));
        b1.setEarliestTime(1);
        b1.setLatestTime(5);
        b1.setDuration(3);
        b1.setPrice(BigDecimal.valueOf(39.54893430891422));


        // Bid 2
        SubbidCtx sbc21_1 = new SubbidCtx();
        sbc21_1.setVmType("v1");
        sbc21_1.setVmSize(32);

        SubbidCtx sbc21_2 = new SubbidCtx();
        sbc21_2.setVmType("v4");
        sbc21_2.setVmSize(32);

        Subbid sb21 = new Subbid();
        sb21.setVmAlternatives(Arrays.asList(sbc21_1, sbc21_2));
        sb21.setQuantity(1);

        SubbidCtx sbc22_1 = new SubbidCtx();
        sbc22_1.setVmType("v3");
        sbc22_1.setVmSize(16);

        Subbid sb22 = new Subbid();
        sb22.setVmAlternatives(Arrays.asList(sbc22_1));
        sb22.setQuantity(1);


        Bid b2 = new Bid();
        b2.setSubbidList(Arrays.asList(sb21, sb22));
        b2.setEarliestTime(1);
        b2.setLatestTime(5);
        b2.setDuration(2);
        b2.setPrice(BigDecimal.valueOf(27.115903193772866));


        // Bid 3
        SubbidCtx sbc31_1 = new SubbidCtx();
        sbc31_1.setVmType("v5");
        sbc31_1.setVmSize(48);

        Subbid sb31 = new Subbid();
        sb31.setVmAlternatives(Arrays.asList(sbc31_1));
        sb31.setQuantity(2);

        SubbidCtx sbc32_1 = new SubbidCtx();
        sbc32_1.setVmType("v4");
        sbc32_1.setVmSize(8);

        Subbid sb32 = new Subbid();
        sb32.setVmAlternatives(Arrays.asList(sbc32_1));
        sb32.setQuantity(1);

        SubbidCtx sbc33_1 = new SubbidCtx();
        sbc33_1.setVmType("v2");
        sbc33_1.setVmSize(48);

        Subbid sb33 = new Subbid();
        sb33.setVmAlternatives(Arrays.asList(sbc33_1));
        sb33.setQuantity(1);

        SubbidCtx sbc34_1 = new SubbidCtx();
        sbc34_1.setVmType("v1");
        sbc34_1.setVmSize(32);

        SubbidCtx sbc34_2 = new SubbidCtx();
        sbc34_2.setVmType("v2");
        sbc34_2.setVmSize(32);

        SubbidCtx sbc34_3 = new SubbidCtx();
        sbc34_3.setVmType("v5");
        sbc34_3.setVmSize(32);

        Subbid sb34 = new Subbid();
        sb34.setVmAlternatives(Arrays.asList(sbc34_1, sbc34_2, sbc34_3));
        sb34.setQuantity(2);

        Bid b3 = new Bid();
        b3.setSubbidList(Arrays.asList(sb31, sb32, sb33, sb34));
        b3.setEarliestTime(1);
        b3.setLatestTime(5);
        b3.setDuration(3);
        b3.setPrice(BigDecimal.valueOf(170.05358513351925));

        bidList.add(b1);
        bidList.add(b2);
        bidList.add(b3);

        TestCase tc = new TestCase();
        tc.setTestCaseName("RealCase3_Greedy");
        tc.setBids(bidList);
        tc.setPhysicalMachines(physicalMachineManager.getPhysicalMachineMap().get(512));
        tc.setPeriod(5);

        return tc;
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
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
        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, "O4");
        System.out.println("# OF SECS:" + ts.getTimeElapsedSeconds());

        Assertions.assertEquals(true, verifierManager.isFeasible(ti.testCase, ts));
    }
}
