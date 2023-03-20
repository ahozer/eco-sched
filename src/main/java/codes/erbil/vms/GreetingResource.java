package codes.erbil.vms;

import codes.erbil.vms.config.generator.TestGenerator;
import codes.erbil.vms.config.manager.PhysicalServerConfigManager;
import codes.erbil.vms.config.manager.TestCaseManager;
import codes.erbil.vms.config.manager.VirtualMachineTypeManager;
import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.service.TestInstanceService;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/hello")
public class GreetingResource {

    @Inject
    RandomGenerator rand;

    @Inject
    VirtualMachineTypeManager virtualMachineTypeManager;

    @Inject
    PhysicalServerConfigManager physicalServerConfigManager;

    @Inject
    TestCaseManager testCaseManager;

    @Inject
    EcoSchedGurobiSolver ecoSchedGurobiSolver;

    @Inject
    TestInstanceService testInstanceService;

    @Inject
    TestGenerator testGenerator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Bid> hello() throws IOException {
        NormalDistribution durationDistribution = new NormalDistribution(rand, 0, 3);

        Class clazz = GreetingResource.class;
        InputStream inputStream = clazz.getResourceAsStream("/pm_100.txt");

        //return durationDistribution.sample();
        //return readFromInputStream(inputStream);
        //return physicalServerConfigManager.getPscList();
        //return testCaseManager.getToyTestCase_1();

        //testInstanceService.saveTestInstanceDb();

        //ecoSchedGurobiSolver.solve(testCaseManager.getToyTestCase_2());
        //ecoSchedGurobiSolver.solve(testInstanceService.getTestInstanceDb().testCase);
        //return "Hello";
        //return virtualMachineTypeManager.getVmTypes();

        return testGenerator.generate();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TestCase hello(TestCase tc) {
        System.out.println(tc);
        return tc;
    }

}