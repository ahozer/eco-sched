package codes.erbil.vms.config.manager;

import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.*;
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
public class SolverManager {

    @ConfigProperty(name = "vms.solver.threadcount")
    Integer threadCount;

    @Inject
    EcoSchedGurobiSolver ecoSchedGurobiSolver;

    @Inject
    GreedySolver greedySolver;

    @Inject
    SingleBidPlacementSolver singleBidPlacementSolver;

    @Inject
    MultipleBidPlacementSolver multipleBidPlacementSolver;

    @Inject
    GeneticSolver geneticSolver;

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

        TestSolution ts = ecoSchedGurobiSolver.solve(ti.testCase);

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, order);

        TestSolution ts = greedySolver.solve(ti.testCase, orderedBidIndexes, order);

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

    // ######## SINGLE BID PLACEMENT FIXED ########
    public void sbpfSolverJobStarter(String testFamilyName, Long testSetNo, String order) {
        managedExecutor.execute(threadContext.contextualRunnable(() -> sbpfSolverJob(testFamilyName, testSetNo, order)));
    }

    private void sbpfSolverJob(String testFamilyName, Long testSetNo, String order) {
        System.out.println("SingleBidPlacementJob - Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        ExecutorService solverExecutorService = null;

        try {
            solverExecutorService = Executors.newFixedThreadPool(threadCount);

            List<TestInstance> instancesForFamilyAndSet = TestInstance.find(
                    "testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                    Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo)).list();

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "SBPF-" + order);

            System.out.println("There are " + unsolvedInstances.size() +
                    " unsolved test cases for Test Family:" + testFamilyName + " and Test Set No:" + testSetNo);

            for (TestInstance ti : unsolvedInstances) {
                solverExecutorService.execute(threadContext.contextualRunnable(() -> solveWithSbpfAndSaveTheResultJob(ti, order)));
            }

        } finally {
            solverExecutorService.shutdown();
        }
    }

    private void solveWithSbpfAndSaveTheResultJob(TestInstance ti, String order) {
        System.out.println("Thread'in adı:" + Thread.currentThread().getName() + ", ID:" + Thread.currentThread().getId());

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, order);

        TestSolution ts = singleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, true, order);

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

    // ######## MULTIPLE BID PLACEMENT FIXED ########
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

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "MBP-" + order + "-B" + multipleBidPlacementSolver.getMbpBatchSize());

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

        List<Integer> orderedBidIndexes = greedySolver.orderBids(ti.testCase, order);

        TestSolution ts = multipleBidPlacementSolver.solve(ti.testCase, orderedBidIndexes, order);

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

            List<TestInstance> unsolvedInstances = getUnsolvedForAlgorithm(instancesForFamilyAndSet, "GENETIC-P" + geneticSolver.getPopulationSize());

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

        TestSolution ts = geneticSolver.solve(ti.testCase, "GREEDY", "O4");

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


    public void removeDuplicateSolutions(String testFamilyName, Long testSetNo, String strategy) {
        List<TestInstance> instancesForFamilyAndSet = TestInstance.find("{\"solutions.solutionStrategy\": \"" + strategy +
                "\", \"testFamilyName\":\"" + testFamilyName + "\", \"testSetNo\":" + testSetNo + "}").list();

        System.out.println(strategy + " çözüme sahip TI Sayısı:" + instancesForFamilyAndSet.size());

        for (TestInstance ti : instancesForFamilyAndSet) {
            int i = 0;
            for (TestSolution ts : ti.solutions) {
                if (strategy.equals(ts.getSolutionStrategy())) {
                    break;
                }

                i++;
            }

            ti.solutions.remove(i);
        }

        TestInstance.persistOrUpdate(instancesForFamilyAndSet);

        System.out.println("Removed duplicate solutions");
    }
}