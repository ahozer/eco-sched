package codes.erbil.vms.service;

import codes.erbil.vms.config.generator.TestGenerator;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestInstance;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TestGeneratorService {

    @Inject
    TestGenerator testGenerator;

    @ConfigProperty(name = "vms.seed")
    Integer seed;

    @Inject
    TestInstanceService testInstanceService;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    ThreadContext threadContext;

    public void generatorJobStarter(String testFamilyName) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> testCaseGenerationJob(testFamilyName)));
    }

    private void testCaseGenerationJob(String testFamilyName) {
        System.out.println("Merhaba bu thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        Long testSetNo = testInstanceService.getMaxTestSetNoOfGivenFamilyName(testFamilyName) + 1;

        List<TestCase> generatedTestCases = testGenerator.generate();

        List<TestInstance> testInstances = generatedTestCases.stream()
                .map(tc -> testInstanceService.buildTestInstanceFromTestCase(tc, seed, false, testFamilyName, testSetNo))
                .collect(Collectors.toList());

        testInstanceService.saveTestInstancesToDb(testInstances);
    }

    public void deletionJobStarter(String testFamilyName, Long testSetNo) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> testCaseDeletionJob(testFamilyName, testSetNo)));
    }

    private void testCaseDeletionJob(String testFamilyName, Long testSetNo) {
        System.out.println("Merhaba bu thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        testInstanceService.removeTestInstancesWithFamilyNameAndSetNo(testFamilyName, testSetNo);
    }

    public Long testInstancesCount() {
        return testInstanceService.testInstancesCount();
    }
}
