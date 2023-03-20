package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.PhysicalMachineManager;
import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.Subbid;
import codes.erbil.vms.config.model.SubbidCtx;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

@QuarkusTest
class GreedySolverTest {

    @Inject
    PhysicalMachineManager physicalMachineManager;

    @InjectSpy
    VerifierManager verifierManager;

    @Inject
    GreedySolver greedySolver;

    @BeforeEach
    void setUp() {
    }

    @Test
    void solve_availableTimeAndMachineScenario() {
        TestCase tc = createTestCaseForGreedy_1();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_1"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=2]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=1][t=1][i=1][j=1][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=1][t=1][i=1][j=1][k=1]"));
    }

    @Test
    void solve_availableMachineScenario() {
        TestCase tc = createTestCaseForGreedy_1();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_2"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=2]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=2][t=1][i=1][j=1][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=2][t=1][i=1][j=1][k=1]"));
    }

    @Test
    void solve_nextAvailableTimeSlotScenario() {
        TestCase tc = createTestCaseForGreedy_1();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_3"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=4]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=5]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=1][t=3][i=1][j=1][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=1][t=3][i=1][j=1][k=1]"));
    }

    @Test
    void solve_RollBackVmAlternativeScenario() {
        TestCase tc = createTestCaseForGreedy_1();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_4"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=4]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=5]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=1][t=3][i=1][j=1][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=1][t=3][i=1][j=1][k=1]"));
    }

    @Test
    void solve_RollBackSubbidScenario() {
        TestCase tc = createTestCaseForGreedy_2();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_5"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=3]"));

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=4]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=5]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=4]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=5]"));


        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=1][t=3][i=1][j=1][k=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=2][t=3][i=1][j=2][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=1][t=3][i=1][j=1][k=1]"));
        Assertions.assertEquals(Double.valueOf(1.00), ts.getDecisionVariables().get("z[a=2][t=3][i=1][j=2][k=1]"));
    }

    @Test
    void solve_rejectInfeasibleBidsByPrice() {
        TestCase tc = createTestCaseForGreedy_2();
        tc.getBids().get(0).setPrice(BigDecimal.valueOf(5)); // Bid fiyatını $5 yap

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_1"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(null, ts.getDecisionVariables());
    }

    @Test
    void solve_basicMultipleBids() {
        TestCase tc = createTestCaseForGreedy_3();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_1"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[2]"));

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=2][t=1]"));

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=2]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=2]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=3][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=3][t=2]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=3][t=3]"));


        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=1][t=1][i=1][j=1][k=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=2][t=1][i=1][j=2][k=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=3][t=1][i=2][j=1][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=1][t=1][i=1][j=1][k=1]"));
        Assertions.assertEquals(Double.valueOf(1.00), ts.getDecisionVariables().get("z[a=2][t=1][i=1][j=2][k=1]"));
        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=3][t=1][i=2][j=1][k=1]"));
    }

    @Test
    void solve_basicMultipleBids_timeDifferences() {
        TestCase tc = createTestCaseForGreedy_3();

        Mockito.when(verifierManager.initializeMachineTimeMap(tc))
                .thenReturn(initializeMachineTimeMapForTest(tc, "Scenario_6"));

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("x[2]"));

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("y[i=2][t=1]"));

        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=4]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=1][t=5]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=3]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=4]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=2][t=5]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=3][t=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=3][t=2]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("o[a=3][t=3]"));


        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=1][t=3][i=1][j=1][k=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=2][t=3][i=1][j=2][k=1]"));
        Assertions.assertEquals(true, ts.getDecisionVariables().containsKey("z[a=3][t=1][i=2][j=1][k=1]"));

        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=1][t=3][i=1][j=1][k=1]"));
        Assertions.assertEquals(Double.valueOf(1.00), ts.getDecisionVariables().get("z[a=2][t=3][i=1][j=2][k=1]"));
        Assertions.assertEquals(Double.valueOf(2.00), ts.getDecisionVariables().get("z[a=3][t=1][i=2][j=1][k=1]"));
    }

    @Test
    void solve_realTestCase1() {
        System.out.println("AVMC=512, BD=0.25, T=5, VM ALTER=1, VM QTY=1, SUBBID COUNT=1");

        TestCase tc = createTestCaseForGreedy_RealCase1();

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    //@Test
    void solve_realTestCase1_defaultOrder() {
        TestInstance ti = TestInstance.findByName("AVMC1024_T20_BD3.0_SBC3_A2_Q1").get(0);

        TestCase tc = ti.testCase;

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "DEFAULT");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "DEFAULT");

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    //@Test
    void solve_realTestCase1_linearRelaxedOrder() {
        TestCase tc = createTestCaseForGreedy_RealCase1();

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "O1");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "O1");

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    @Test
    void solve_realTestCase1_avgProfitOrder() {
        TestCase tc = createTestCaseForGreedy_RealCase1();

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "O3");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "O3");

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    @Test
    void solve_realTestCase1_avgProfitOrderEnergy() {
        TestCase tc = createTestCaseForGreedy_RealCase1();

        List<Integer> orderedBidIndexes = greedySolver.orderBids(tc, "O4");
        TestSolution ts = greedySolver.solve(tc, orderedBidIndexes, "O4");

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    private TestCase createTestCaseForGreedy_1() {
        List<Bid> bidList = new ArrayList<>();

        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v1");
        sbc11_1.setVmSize(24);

        SubbidCtx sbc11_2 = new SubbidCtx();
        sbc11_2.setVmType("v2");
        sbc11_2.setVmSize(24);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1, sbc11_2));
        sb11.setQuantity(2);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11));
        b1.setEarliestTime(1);
        b1.setLatestTime(5);
        b1.setDuration(3);
        b1.setPrice(BigDecimal.valueOf(50.0));

        bidList.add(b1);

        TestCase tc = new TestCase();
        tc.setTestCaseName("TestCase1_Greedy");
        tc.setBids(bidList);
        tc.setPhysicalMachines(physicalMachineManager.getPhysicalMachineMap().get(1024));
        tc.setPeriod(5);

        return tc;
    }

    private TestCase createTestCaseForGreedy_2() {
        List<Bid> bidList = new ArrayList<>();

        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v1");
        sbc11_1.setVmSize(24);

        SubbidCtx sbc11_2 = new SubbidCtx();
        sbc11_2.setVmType("v2");
        sbc11_2.setVmSize(24);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1, sbc11_2));
        sb11.setQuantity(2);

        SubbidCtx sbc21_1 = new SubbidCtx();
        sbc21_1.setVmType("v1");
        sbc21_1.setVmSize(48);

        Subbid sb21 = new Subbid();
        sb21.setVmAlternatives(Arrays.asList(sbc21_1));
        sb21.setQuantity(1);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11, sb21));
        b1.setEarliestTime(1);
        b1.setLatestTime(5);
        b1.setDuration(3);
        b1.setPrice(BigDecimal.valueOf(50.0));

        bidList.add(b1);

        TestCase tc = new TestCase();
        tc.setTestCaseName("TestCase2_Greedy");
        tc.setBids(bidList);
        tc.setPhysicalMachines(physicalMachineManager.getPhysicalMachineMap().get(1024));
        tc.setPeriod(5);

        return tc;
    }

    private TestCase createTestCaseForGreedy_3() {
        List<Bid> bidList = new ArrayList<>();

        // Bid 1
        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v1");
        sbc11_1.setVmSize(24);

        SubbidCtx sbc11_2 = new SubbidCtx();
        sbc11_2.setVmType("v2");
        sbc11_2.setVmSize(24);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1, sbc11_2));
        sb11.setQuantity(2);

        SubbidCtx sbc12_1 = new SubbidCtx();
        sbc12_1.setVmType("v1");
        sbc12_1.setVmSize(48);

        Subbid sb12 = new Subbid();
        sb12.setVmAlternatives(Arrays.asList(sbc12_1));
        sb12.setQuantity(1);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11, sb12));
        b1.setEarliestTime(1);
        b1.setLatestTime(5);
        b1.setDuration(3);
        b1.setPrice(BigDecimal.valueOf(50.0));

        // Bid 2
        SubbidCtx sbc21_1 = new SubbidCtx();
        sbc21_1.setVmType("v2");
        sbc21_1.setVmSize(48);

        Subbid sb21 = new Subbid();
        sb21.setVmAlternatives(Arrays.asList(sbc21_1));
        sb21.setQuantity(2);

        Bid b2 = new Bid();
        b2.setSubbidList(Arrays.asList(sb21));
        b2.setEarliestTime(1);
        b2.setLatestTime(5);
        b2.setDuration(3);
        b2.setPrice(BigDecimal.valueOf(50.0));

        bidList.add(b1);
        bidList.add(b2);

        TestCase tc = new TestCase();
        tc.setTestCaseName("TestCase3_Greedy");
        tc.setBids(bidList);
        tc.setPhysicalMachines(physicalMachineManager.getPhysicalMachineMap().get(1024));
        tc.setPeriod(5);

        return tc;
    }

    private TestCase createTestCaseForGreedy_RealCase1() {
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


    public Map<Integer, Map<Integer, Integer>> initializeMachineTimeMapForTest(TestCase tc, String testScenario) {
        Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime = new HashMap<>();

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            Map<Integer, Integer> timeMap = new HashMap<>();

            utilizationOfMachineAtTime.put(a, timeMap);
            for (int t = 1; t <= tc.getPeriod(); t++) {
                timeMap.put(t, 0);
            }
        }

        // Scenario 1 is same as empty

        if ("Scenario_2".equalsIgnoreCase(testScenario)) {
            utilizationOfMachineAtTime.get(1).replace(2, 32);
            utilizationOfMachineAtTime.get(3).replace(2, 96);
        } else if ("Scenario_3".equalsIgnoreCase(testScenario)) {
            utilizationOfMachineAtTime.get(1).replace(2, 32);
            utilizationOfMachineAtTime.get(2).replace(2, 32);

            utilizationOfMachineAtTime.get(3).replace(2, 96);
            utilizationOfMachineAtTime.get(4).replace(2, 96);
        } else if ("Scenario_4".equalsIgnoreCase(testScenario)) {
            utilizationOfMachineAtTime.get(1).replace(2, 32);
            utilizationOfMachineAtTime.get(2).replace(2, 24);

            utilizationOfMachineAtTime.get(3).replace(2, 96);
            utilizationOfMachineAtTime.get(4).replace(2, 96);
        } else if ("Scenario_5".equalsIgnoreCase(testScenario)) {
            utilizationOfMachineAtTime.get(1).replace(2, 24);
            utilizationOfMachineAtTime.get(2).replace(2, 32);

            utilizationOfMachineAtTime.get(4).replace(2, 96);
        } else if ("Scenario_6".equalsIgnoreCase(testScenario)) {
            utilizationOfMachineAtTime.get(1).replace(2, 48);
            utilizationOfMachineAtTime.get(2).replace(2, 48);
        }

        return utilizationOfMachineAtTime;
    }
}