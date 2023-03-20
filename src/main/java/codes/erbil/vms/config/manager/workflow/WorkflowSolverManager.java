package codes.erbil.vms.config.manager.workflow;

import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.workflow.WorkflowGeneticSolver;
import codes.erbil.vms.solver.workflow.WorkflowGreedySolver;
import codes.erbil.vms.solver.workflow.WorkflowGurobiSolver;
import codes.erbil.vms.solver.workflow.WorkflowMultipleBidPlacementSolver;
import io.quarkus.panache.common.Parameters;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class WorkflowSolverManager {

    @ConfigProperty(name = "vms.solver.threadcount")
    Integer threadCount;

    @Inject
    WorkflowGurobiSolver workflowGurobiSolver;

    @Inject
    WorkflowGreedySolver workflowGreedySolver;

    @Inject
    WorkflowMultipleBidPlacementSolver workflowMultipleBidPlacementSolver;

    @Inject
    WorkflowGeneticSolver workflowGeneticSolver;

    @Inject
    VerifierManager verifierManager;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    ThreadContext threadContext;

    // ######## GUROBI ########
    public void gurobiSolverJobStarter(String testFamilyName, Long testSetNo) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> gurobiSolverJob(testFamilyName, testSetNo)));
    }

    private void gurobiSolverJob(String testFamilyName, Long testSetNo) {
        System.out.println("GurobiSolverJob - Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        ExecutorService solverExecutorService = null;

        try {
            solverExecutorService = Executors.newFixedThreadPool(threadCount);

            List<TestInstance> instancesForFamilyAndSet = TestInstance.find(
                    "testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                    Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo)).list();

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "GUROBI");

            System.out.println("There are " + unsolvedInstances.size() +
                    " unsolved test cases for Test Family:" + testFamilyName + " and Test Set No:" + testSetNo);


            for (TestInstance ti : unsolvedInstances) {
                solverExecutorService.execute(threadContext.contextualRunnable(() -> solveWithGurobiAndSaveTheResultJob(ti)));
            }

        } finally {
            solverExecutorService.shutdown();
        }
    }

    private void solveWithGurobiAndSaveTheResultJob(TestInstance ti) {
        System.out.println("Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        TestSolution ts = workflowGurobiSolver.solve(ti.testCase);

        ts.setFeasible(verifierManager.isFeasible(ti.testCase, ts));

        if (ti.solutions == null) {
            ti.solutions = new ArrayList<>();
        }

        ti.solutions.add(ts);
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç doğrulandı ve veritabanına yazılıyor...");
        ti.persistOrUpdate();
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç başarılı bir şekilde veritabanına yazıldı");
    }

    // ######## END GUROBI ########

    // ######## GREEDY ########
    public void greedySolverJobStarter(String testFamilyName, Long testSetNo, String order) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> greedySolverJob(testFamilyName, testSetNo, order)));
    }

    private void greedySolverJob(String testFamilyName, Long testSetNo, String order) {
        System.out.println("Greedy - Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        ExecutorService solverExecutorService = null;

        try {
            solverExecutorService = Executors.newFixedThreadPool(threadCount);

            List<TestInstance> instancesForFamilyAndSet = TestInstance.find(
                    "testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                    Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo)).list();

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "GREEDY-" + order);

            System.out.println("There are " + unsolvedInstances.size() +
                    " unsolved test cases for Test Family:" + testFamilyName + " and Test Set No:" + testSetNo);

            for (TestInstance ti : unsolvedInstances) {
                solverExecutorService.execute(threadContext.contextualRunnable(() -> solveWithGreedyAndSaveTheResultJob(ti, order)));
            }

        } finally {
            solverExecutorService.shutdown();
        }
    }

    private void solveWithGreedyAndSaveTheResultJob(TestInstance ti, String order) {
        System.out.println("Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        List<Integer> orderedWorkflowIndexes = workflowGreedySolver.orderWorkflows(ti.testCase, order);

        TestSolution ts = workflowGreedySolver.solve(ti.testCase, orderedWorkflowIndexes, order);

        ts.setFeasible(verifierManager.isFeasible(ti.testCase, ts));

        if (ti.solutions == null) {
            ti.solutions = new ArrayList<>();
        }

        ti.solutions.add(ts);
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç doğrulandı ve veritabanına yazılıyor...");
        ti.persistOrUpdate();
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç başarılı bir şekilde veritabanına yazıldı");
    }

    // ######## END GREEDY ########

    // ######## MBP ########
    public void mbpSolverJobStarter(String testFamilyName, Long testSetNo, String order) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> mbpSolverJob(testFamilyName, testSetNo, order)));
    }

    private void mbpSolverJob(String testFamilyName, Long testSetNo, String order) {
        System.out.println("MultipleBidPlacementJob - Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        ExecutorService solverExecutorService = null;

        try {
            solverExecutorService = Executors.newFixedThreadPool(threadCount);

            List<TestInstance> instancesForFamilyAndSet = TestInstance.find(
                    "testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                    Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo)).list();

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "MBP-" + order + "-B" + workflowMultipleBidPlacementSolver.getMbpBatchSize());

            System.out.println("There are " + unsolvedInstances.size() +
                    " unsolved test cases for Test Family:" + testFamilyName + " and Test Set No:" + testSetNo);

            for (TestInstance ti : unsolvedInstances) {
                solverExecutorService.execute(threadContext.contextualRunnable(() -> solveWithMbpAndSaveTheResultJob(ti, order)));
            }

        } finally {
            solverExecutorService.shutdown();
        }
    }

    private void solveWithMbpAndSaveTheResultJob(TestInstance ti, String order) {
        System.out.println("Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        List<Integer> orderedWorkflowIndexes = workflowGreedySolver.orderWorkflows(ti.testCase, order);

        TestSolution ts = workflowMultipleBidPlacementSolver.solve(ti.testCase, orderedWorkflowIndexes, order);

        ts.setFeasible(verifierManager.isFeasible(ti.testCase, ts));

        if (ti.solutions == null) {
            ti.solutions = new ArrayList<>();
        }

        ti.solutions.add(ts);
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç doğrulandı ve veritabanına yazılıyor...");
        ti.persistOrUpdate();
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç başarılı bir şekilde veritabanına yazıldı");
    }
    // ######## END MBP ########

    // # GENETIC ALGORITHM
    public void geneticSolverJobStarter(String testFamilyName, Long testSetNo) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> geneticSolverJob(testFamilyName, testSetNo)));
    }

    private void geneticSolverJob(String testFamilyName, Long testSetNo) {
        System.out.println("Genetic Algorithm - Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        ExecutorService solverExecutorService = null;

        try {
            solverExecutorService = Executors.newFixedThreadPool(threadCount);

            List<TestInstance> instancesForFamilyAndSet = TestInstance.find(
                    "testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                    Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo)).list();

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "GENETIC-P" + workflowGeneticSolver.getPopulationSize());

            System.out.println("There are " + unsolvedInstances.size() +
                    " unsolved test cases for Test Family:" + testFamilyName + " and Test Set No:" + testSetNo);

            for (TestInstance ti : unsolvedInstances) {
                solverExecutorService.execute(threadContext.contextualRunnable(() -> solveWithGeneticAndSaveTheResultJob(ti)));
            }

        } finally {
            solverExecutorService.shutdown();
        }
    }

    private void solveWithGeneticAndSaveTheResultJob(TestInstance ti) {
        System.out.println("Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        TestSolution ts = workflowGeneticSolver.solve(ti.testCase, "GREEDY", "O4");

        ts.setFeasible(verifierManager.isFeasible(ti.testCase, ts));

        if (ti.solutions == null) {
            ti.solutions = new ArrayList<>();
        }

        ti.solutions.add(ts);
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç doğrulandı ve veritabanına yazılıyor...");
        ti.persistOrUpdate();
        System.out.println("[TC NAME:" + ti.testCaseName + ", TC FAMILY:" + ti.testFamilyName + " SET NO:" + ti.testSetNo + "]" +
                " için sonuç başarılı bir şekilde veritabanına yazıldı");
    }

    // ######## END GENETIC ALGORITHM ########

    private List<TestInstance> getUnsolvedForAlgorithm(List<TestInstance> instances, String strategy) {
        List<TestInstance> tiList = new ArrayList<>();

        for (TestInstance instance : instances) {
            if (instance.solutions == null) {
                tiList.add(instance);
            } else {
                boolean exists = false;
                for (TestSolution ts : instance.solutions) {
                    if (strategy.equals(ts.getSolutionStrategy())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) tiList.add(instance);
            }
        }

        return tiList;
    }
}
