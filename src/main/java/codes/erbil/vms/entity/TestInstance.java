package codes.erbil.vms.entity;

import codes.erbil.vms.config.model.TestCase;
import io.quarkus.mongodb.panache.PanacheMongoEntity;

import java.time.LocalDate;
import java.util.List;

public class TestInstance extends PanacheMongoEntity {
    public String testFamilyName;
    public Long testSetNo;

    public String testCaseName;
    public LocalDate createdDate;
    public Integer randomSeed;

    public boolean workflow;
    public TestCase testCase;
    public List<TestSolution> solutions;

    public static List<TestInstance> findByName(String testCaseName) {
        return list("testCaseName", testCaseName);
    }
}
