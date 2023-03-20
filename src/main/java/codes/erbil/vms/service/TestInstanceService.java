package codes.erbil.vms.service;

import codes.erbil.vms.config.manager.TestCaseManager;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestInstance;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TestInstanceService {

    @Inject
    TestCaseManager testCaseManager;


    public void saveTestInstancesToDb(List<TestInstance> tiList) {
        // SAVING TO DB
        System.out.println("Üretilen testler veritabanına kaydediliyor...");
        TestInstance.persist(tiList);
        System.out.println("Başarıyla kaydedildi.");
    }

    public void removeAllTestInstancesFromDb() {
        System.out.println("Veritabanındaki bütün testler siliniyor...");
        Long deletedCount = TestInstance.deleteAll();
        System.out.println("Veritabanındaki " + deletedCount + " test başarıyla silindi.");
    }

    public void removeTestInstancesWithFamilyName(String testFamilyName) {
        System.out.println("Veritabanındaki " + testFamilyName + " ailesine ait bütün testler siliniyor...");

        Long deletedCount = TestInstance.delete("testFamilyName", testFamilyName);


        System.out.println("Veritabanındaki " + deletedCount + " test başarıyla silindi.");
    }

    public void removeTestInstancesWithFamilyNameAndSetNo(String testFamilyName, Long testSetNo) {
        System.out.println("Veritabanındaki " + testFamilyName + " ailesine ve " + testSetNo + ". sete ait bütün testler siliniyor...");

        Long deletedCount = TestInstance.delete("testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo));

        System.out.println("Veritabanındaki " + deletedCount + " test başarıyla silindi.");
    }

    public Long testInstancesCount() {
        return TestInstance.count();
    }

    public List<TestInstance> getTestInstancesByName(String testCaseName) {
        return TestInstance.findByName(testCaseName);
    }

    public TestInstance buildTestInstanceFromTestCase(TestCase tc, Integer randomSeed, boolean isWorkFlow, String testFamilyName, Long testSetNo) {
        TestInstance ti = new TestInstance();

        ti.testCase = tc;
        ti.testCaseName = tc.getTestCaseName();
        ti.randomSeed = randomSeed;
        ti.workflow = isWorkFlow;
        ti.createdDate = LocalDate.now();
        ti.testFamilyName = testFamilyName;
        ti.testSetNo = testSetNo;

        return ti;
    }

    public void saveTestInstanceDb() {
        TestInstance ti = new TestInstance();

        ti.randomSeed = 27;
        ti.createdDate = LocalDate.now();
        ti.workflow = false;
        ti.testCaseName = "ToyTestCase1";
        ti.testCase = testCaseManager.getToyTestCase_1();

        ti.persist();
    }

    public Long getMaxTestSetNoOfGivenFamilyName(String testFamilyName) {
        Optional<TestInstance> ti = TestInstance.find("testFamilyName", Sort.by("testSetNo").descending(), testFamilyName)
                .firstResultOptional();

        return ti.isPresent() ? ti.get().testSetNo : 0;
    }
}
