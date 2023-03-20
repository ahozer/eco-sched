package codes.erbil.vms.solver;

import codes.erbil.vms.config.manager.PhysicalMachineManager;
import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.Subbid;
import codes.erbil.vms.config.model.SubbidCtx;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestSolution;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class GurobiSolverTest {

    @Inject
    PhysicalMachineManager physicalMachineManager;

    @InjectSpy
    VerifierManager verifierManager;

    @Inject
    EcoSchedGurobiSolver ecoSchedGurobiSolver;

    @Test
    void solve_thesisExample() {
        TestCase tc = createTestCase();

        TestSolution ts = ecoSchedGurobiSolver.solve(tc);

        Assertions.assertEquals(true, verifierManager.isFeasible(tc, ts));
    }

    private TestCase createTestCase() {
        List<Bid> bidList = new ArrayList<>();

        // Bid 1
        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v1");
        sbc11_1.setVmSize(32);

        SubbidCtx sbc11_2 = new SubbidCtx();
        sbc11_2.setVmType("v5");
        sbc11_2.setVmSize(32);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1, sbc11_2));
        sb11.setQuantity(3);

        SubbidCtx sbc12_1 = new SubbidCtx();
        sbc12_1.setVmType("v4");
        sbc12_1.setVmSize(24);

        SubbidCtx sbc12_2 = new SubbidCtx();
        sbc12_2.setVmType("v7");
        sbc12_2.setVmSize(24);

        Subbid sb12 = new Subbid();
        sb12.setVmAlternatives(Arrays.asList(sbc12_1, sbc12_2));
        sb12.setQuantity(2);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11, sb12));
        b1.setEarliestTime(1);
        b1.setLatestTime(10);
        b1.setDuration(7);
        b1.setPrice(BigDecimal.valueOf(200.0));

        // Bid 2
        SubbidCtx sbc21_1 = new SubbidCtx();
        sbc21_1.setVmType("v2");
        sbc21_1.setVmSize(24);

        Subbid sb21 = new Subbid();
        sb21.setVmAlternatives(Arrays.asList(sbc21_1));
        sb21.setQuantity(2);

        SubbidCtx sbc22_1 = new SubbidCtx();
        sbc22_1.setVmType("v1");
        sbc22_1.setVmSize(24);

        Subbid sb22 = new Subbid();
        sb22.setVmAlternatives(Arrays.asList(sbc22_1));
        sb22.setQuantity(2);

        Bid b2 = new Bid();
        b2.setSubbidList(Arrays.asList(sb21, sb22));
        b2.setEarliestTime(1);
        b2.setLatestTime(10);
        b2.setDuration(3);
        b2.setPrice(BigDecimal.valueOf(100.0));

        // Bid 3
        SubbidCtx sbc31_1 = new SubbidCtx();
        sbc31_1.setVmType("v2");
        sbc31_1.setVmSize(24);

        SubbidCtx sbc31_2 = new SubbidCtx();
        sbc31_2.setVmType("v5");
        sbc31_2.setVmSize(24);

        Subbid sb31 = new Subbid();
        sb31.setVmAlternatives(Arrays.asList(sbc31_1, sbc31_2));
        sb31.setQuantity(2);

        Bid b3 = new Bid();
        b3.setSubbidList(Arrays.asList(sb31));
        b3.setEarliestTime(1);
        b3.setLatestTime(10);
        b3.setDuration(3);
        b3.setPrice(BigDecimal.valueOf(150.0));

        // Bid 4
        SubbidCtx sbc41_1 = new SubbidCtx();
        sbc41_1.setVmType("v1");
        sbc41_1.setVmSize(16);

        Subbid sb41 = new Subbid();
        sb41.setVmAlternatives(Arrays.asList(sbc41_1));
        sb41.setQuantity(3);

        SubbidCtx sbc42_1 = new SubbidCtx();
        sbc42_1.setVmType("v3");
        sbc42_1.setVmSize(24);

        SubbidCtx sbc42_2 = new SubbidCtx();
        sbc42_2.setVmType("v4");
        sbc42_2.setVmSize(24);

        Subbid sb42 = new Subbid();
        sb42.setVmAlternatives(Arrays.asList(sbc42_1, sbc42_2));
        sb42.setQuantity(4);

        Bid b4 = new Bid();
        b4.setSubbidList(Arrays.asList(sb41, sb42));
        b4.setEarliestTime(1);
        b4.setLatestTime(10);
        b4.setDuration(4);
        b4.setPrice(BigDecimal.valueOf(250.0));

        // Bid 5
        SubbidCtx sbc51_1 = new SubbidCtx();
        sbc51_1.setVmType("v5");
        sbc51_1.setVmSize(24);

        Subbid sb51 = new Subbid();
        sb51.setVmAlternatives(Arrays.asList(sbc51_1));
        sb51.setQuantity(2);

        Bid b5 = new Bid();
        b5.setSubbidList(Arrays.asList(sb51));
        b5.setEarliestTime(1);
        b5.setLatestTime(10);
        b5.setDuration(7);
        b5.setPrice(BigDecimal.valueOf(450.0));

        // Bid 6
        SubbidCtx sbc61_1 = new SubbidCtx();
        sbc61_1.setVmType("v5");
        sbc61_1.setVmSize(24);

        Subbid sb61 = new Subbid();
        sb61.setVmAlternatives(Arrays.asList(sbc61_1));
        sb61.setQuantity(2);

        Bid b6 = new Bid();
        b6.setSubbidList(Arrays.asList(sb61));
        b6.setEarliestTime(1);
        b6.setLatestTime(10);
        b6.setDuration(7);
        b6.setPrice(BigDecimal.valueOf(650.0));

        bidList.add(b1);
        bidList.add(b2);
        bidList.add(b3);
        bidList.add(b4);
        bidList.add(b5);
        bidList.add(b6);

        TestCase tc = new TestCase();
        tc.setTestCaseName("Thesis_Example");
        tc.setBids(bidList);
        tc.setPhysicalMachines(physicalMachineManager.getPhysicalMachineMap().get(512));
        tc.setPeriod(10);

        return tc;
    }
}
