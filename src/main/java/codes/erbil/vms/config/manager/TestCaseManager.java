package codes.erbil.vms.config.manager;

import codes.erbil.vms.config.model.PhysicalMachine;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.util.ConfigFileUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class TestCaseManager {

    private TestCase toyTestCase_1;
    private TestCase toyTestCase_2;

    @Inject
    BidManager bidManager;

    @Inject
    ConfigFileUtil configFileUtil;

    public TestCase getToyTestCase_1() {
        if (toyTestCase_1 == null) {
            readToyTestCases();
        }
        return toyTestCase_1;
    }

    public TestCase getToyTestCase_2() {
        if (toyTestCase_2 == null) {
            readToyTestCases();
        }
        return toyTestCase_2;
    }

    private void readToyTestCases() {
        Class clazz = TestCaseManager.class;
        InputStream inputStreamPm_512 = clazz.getResourceAsStream("/pm_512.txt");
        List<PhysicalMachine> pm512 = new ArrayList<>();
        try {
            pm512 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_512);
        } catch (IOException e) {
            e.printStackTrace();
        }

        toyTestCase_1 = new TestCase();
        toyTestCase_1.setTestCaseName("TestCase1");
        toyTestCase_1.setBids(bidManager.generateToyTestCase1());
        toyTestCase_1.setPhysicalMachines(pm512);
        toyTestCase_1.setPeriod(10);

        toyTestCase_2 = new TestCase();
        toyTestCase_2.setTestCaseName("TestCase2");
        toyTestCase_2.setBids(bidManager.generateToyTestCase2());
        toyTestCase_2.setPhysicalMachines(pm512);
        toyTestCase_2.setPeriod(10);
    }
}
