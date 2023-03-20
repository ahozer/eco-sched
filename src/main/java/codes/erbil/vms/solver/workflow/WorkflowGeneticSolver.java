package codes.erbil.vms.solver.workflow;

import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.model.Individual;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class WorkflowGeneticSolver {
    @ConfigProperty(name = "vms.solver.genetic.tournament.size", defaultValue = "2")
    Integer tournamentSize;

    @ConfigProperty(name = "vms.solver.genetic.iterations", defaultValue = "100")
    Integer numOfIterations;

    @ConfigProperty(name = "vms.solver.genetic.population.size", defaultValue = "20")
    Integer populationSize;

    @ConfigProperty(name = "vms.solver.genetic.mutation.probability", defaultValue = "0.05")
    Double mutationProbability;

    @ConfigProperty(name = "vms.solver.genetic.limit.improvement", defaultValue = "20")
    int improvementLimit;

    @ConfigProperty(name = "vms.solver.genetic.timelimit", defaultValue = "1800")
    int geneticTimeLimit;

    @ConfigProperty(name = "vms.solver.genetic.std.calculation", defaultValue = "true")
    boolean stdCalculationFlag;

    @ConfigProperty(name = "vms.solver.genetic.permsize", defaultValue = "5")
    int permSize;

    @Inject
    WorkflowGreedySolver workflowGreedySolver;

    @Inject
    WorkflowMultipleBidPlacementSolver workflowMultipleBidPlacementSolver;


    @Inject
    RandomGenerator rand;

    @Inject
    Random rd;

    public TestSolution solve(TestCase tc, String placementMethod, String order) {

        Instant start = Instant.now();

        List<Individual> population = new ArrayList<>();

        initializePopulation(tc, population, placementMethod, order);

        Individual bestIndividualSoFar = getBestIndividual(population);


        int improvementCounter = 0;

        // SOLUTION HERE
        for (int i = 1; i <= numOfIterations; i++) {

            doCycle(tc, population, placementMethod, order);

            // FIND BEST SOLUTION
            Individual bestIndPopulation = getBestIndividual(population);
            if (bestIndPopulation.getTestSolution().getObjectiveValue() >
                    bestIndividualSoFar.getTestSolution().getObjectiveValue()) {
                bestIndividualSoFar = bestIndPopulation;

                improvementCounter = 0;
            } else {
                improvementCounter++;
            }

            System.out.println("Iteration #" + i + " | Current best solution fitness:"
                    + bestIndividualSoFar.getTestSolution().getObjectiveValue());

            if (stdCalculationFlag && i % 5 == 0) { // HER 5 ITERASYONDA BIR STDEV BAS
                double[] objVals = population.stream()
                        .map(individual -> individual.getTestSolution().getObjectiveValue())
                        .mapToDouble(Double::doubleValue)
                        .toArray();

                StandardDeviation stdev = new StandardDeviation();
                System.out.println("Population OBJ VAL STDEV:" + stdev.evaluate(objVals));
            }

            if (improvementCounter == improvementLimit) {
                System.out.println("Since no improvement was observed in the last 20 iterations, the algorithm was terminated.");
                break;
            } else if (Duration.between(start, Instant.now()).toSeconds() > geneticTimeLimit) {
                System.out.println("Time limit reached.");
                break;
            }

        }

        // GENETIK ALGORITMADA BULUNAN EN IYI SONUC MBP ILE COZULUR
        System.out.println("GENETIC ALGORITHM RESULTS:");
        TestSolution mbpSolution = workflowMultipleBidPlacementSolver
                .solve(tc, bestIndividualSoFar.getOrderedBidIndexes(), order);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toSeconds();

        TestSolution ts = new TestSolution();
        ts.setSolutionStrategy("GENETIC-P" + populationSize);
        ts.setTimeElapsedSeconds(timeElapsed);
        ts.setMipGap(0.0);

        // MBP İLE KARŞILAŞTIR
        if (mbpSolution.getObjectiveValue() > bestIndividualSoFar.getTestSolution().getObjectiveValue()) {
            System.out.println("MBP daha iyi sonuç veriyor.");
            ts.setObjectiveValue(mbpSolution.getObjectiveValue());
            ts.setDecisionVariables(mbpSolution.getDecisionVariables());
        } else {
            System.out.println("GENETIC'ten bulunan sonuç MBP'den daha iyi.");
            ts.setObjectiveValue(bestIndividualSoFar.getTestSolution().getObjectiveValue());
            ts.setDecisionVariables(bestIndividualSoFar.getTestSolution().getDecisionVariables());
        }

        return ts;
    }

    private void initializePopulation(TestCase tc, List<Individual> population, String placementMethod, String order) {
        System.out.println("Initializing population..");

        List<Integer> firstSolutionOrder = workflowGreedySolver.orderWorkflows(tc, "O1");
        TestSolution ts1 = placementHandler(tc, firstSolutionOrder, placementMethod, "O1");
        Individual firstIndividual = new Individual(firstSolutionOrder, ts1);
        population.add(firstIndividual);

        List<Integer> secondSolutionOrder = firstSolutionOrder.stream().collect(Collectors.toList());
        // Çözmeye gerek yok
        Individual secondIndividual = new Individual(secondSolutionOrder, ts1);
        population.add(secondIndividual);

        List<List<Integer>> partitionedBidIndexes = ListUtils.partition(secondSolutionOrder, permSize);

        while (population.size() < populationSize) {

            List<Integer> randomSolutionOrder = new ArrayList<>();
            for (List<Integer> partitionList : partitionedBidIndexes) {
                List<Integer> toBeShuffled = partitionList.stream().collect(Collectors.toList());
                Collections.shuffle(toBeShuffled, rd);
                randomSolutionOrder.addAll(toBeShuffled);
            }

            TestSolution tsRandom = placementHandler(tc, randomSolutionOrder, placementMethod, order);
            Individual individualRandom = new Individual(randomSolutionOrder, tsRandom);

            population.add(individualRandom);
        }

        System.out.println("Population initialized. Size:" + population.size());
    }

    private Individual getBestIndividual(List<Individual> population) {
        int bestIdx = 0;
        Double bestFitnessVal = Double.MIN_VALUE;

        int idx = 0;
        for (Individual individual : population) {
            if (individual.getTestSolution().getObjectiveValue() > bestFitnessVal) {
                bestFitnessVal = individual.getTestSolution().getObjectiveValue();
                bestIdx = idx;
            }

            idx++;
        }

        return population.get(bestIdx);
    }

    void doCycle(TestCase tc, List<Individual> population, String placementMethod, String order) {

        // PARENT SELECTION
        int firstParentIdx = tournamentSelection(population, tournamentSize);
        int secondParentIdx = tournamentSelection(population, tournamentSize);

        while (firstParentIdx == secondParentIdx) {
            secondParentIdx = tournamentSelection(population, tournamentSize);
        }

        Individual firstParent = population.get(firstParentIdx);
        Individual secondParent = population.get(secondParentIdx);

        // PERFORM CROSSOVER
        crossover(tc, population, firstParent, secondParent, placementMethod, order);

        // PERFORM MUTATION
        // NOTE MUTATION IS NOW PERFORMED INSIDE CROSSOVER OPERATION
        //mutation(tc, population, placementMethod, order);

    }

    private int tournamentSelection(List<Individual> population, int setSize) {
        UniformIntegerDistribution tournamentDistribution = new UniformIntegerDistribution(rand, 0, populationSize - 1);
        int[] subsetIndexes = tournamentDistribution.sample(setSize);

        int winnerIndex = subsetIndexes[0];
        Double winnerFitness = Double.MIN_VALUE;

        for (int idx : subsetIndexes) {
            if (population.get(idx).getTestSolution().getObjectiveValue() > winnerFitness) {
                winnerIndex = idx;
                winnerFitness = population.get(idx).getTestSolution().getObjectiveValue();
            }

        }

        return winnerIndex;
    }

    private void crossover(TestCase tc, List<Individual> population,
                           Individual firstParent, Individual secondParent, String placementMethod, String order) {

        // Get children orders
        List<Integer> child1Order = performCrossover(firstParent, secondParent);
        List<Integer> child2Order = performCrossover(secondParent, firstParent);

        // Evaluate Fitness
        TestSolution tsFirstChild = placementHandler(tc, child1Order, placementMethod, order);
        TestSolution tsSecondChild = placementHandler(tc, child2Order, placementMethod, order);

        // POPÜLASYONDAKİ EN KÖTÜ BİREYİN UÇURULMASI
        int worstIndividualIndex = getWorstIndividualIndex(population);
        population.remove(worstIndividualIndex);

        // POPÜLASYONDAN RASTGELE BİR BİREYIN UÇURULMASI
        UniformIntegerDistribution crossOverReplaceDist = new UniformIntegerDistribution(rand, 0, population.size() - 1);
        Integer selectedIdx = crossOverReplaceDist.sample();
        population.remove(selectedIdx.intValue());

        assert (populationSize - 2) == population.size();

        // ÇOCUKLAR OLUŞTURULUP POPÜLASYONA DAHİL EDİLİR
        Individual child1 = new Individual(child1Order, tsFirstChild);
        Individual child2 = new Individual(child2Order, tsSecondChild);

        population.add(child1);
        population.add(child2);

        assert populationSize == population.size();
    }

    private List<Integer> performCrossover(Individual parent1, Individual parent2) {
        List<Integer> parent1Order = parent1.getOrderedBidIndexes();
        List<Integer> parent2Order = parent2.getOrderedBidIndexes();

        Set<Integer> remainingSet = IntStream.rangeClosed(1, parent1Order.size()).boxed().collect(Collectors.toSet());

        List<Integer> childOrder = new ArrayList<>();

        int pivotIdx = 0;
        while (pivotIdx < parent1Order.size()) {
            int chosenOne = -1;
            int chosenTwo = -1;

            if (rd.nextDouble() > 0.5) { // Parent 1 den seç
                chosenOne = parent1Order.get(pivotIdx);

                if (remainingSet.contains(chosenOne)) {
                    childOrder.add(chosenOne);
                    remainingSet.remove(chosenOne);
                }

                chosenTwo = parent2Order.get(pivotIdx);
                if (remainingSet.contains(chosenTwo)) {
                    childOrder.add(chosenTwo);
                    remainingSet.remove(chosenTwo);
                }

            } else { // Parent 2 den seç
                chosenOne = parent2Order.get(pivotIdx);

                if (remainingSet.contains(chosenOne)) {
                    childOrder.add(chosenOne);
                    remainingSet.remove(chosenOne);
                }

                chosenTwo = parent1Order.get(pivotIdx);
                if (remainingSet.contains(chosenTwo)) {
                    childOrder.add(chosenTwo);
                    remainingSet.remove(chosenTwo);
                }
            }

            pivotIdx++;
        }

        // Place remaining
        childOrder.addAll(remainingSet);

        // PERFORM MUTATION
        if (rd.nextDouble() < mutationProbability) { // APPLY MUTATION
            List<List<Integer>> partitionedOrderList = ListUtils.partition(childOrder, permSize);

            for (List<Integer> partition : partitionedOrderList) {
                UniformIntegerDistribution bidSelDist = new UniformIntegerDistribution(rand, 0, partition.size() - 1);

                Integer firstWorkflowIdx = bidSelDist.sample();
                Integer firstVal = partition.get(firstWorkflowIdx);

                Integer secondWorkflowIdx = bidSelDist.sample();
                Integer secondVal = partition.get(secondWorkflowIdx);

                partition.set(firstWorkflowIdx, secondVal);
                partition.set(secondWorkflowIdx, firstVal);
            }
        }


        return childOrder;
    }

    private int getWorstIndividualIndex(List<Individual> population) {
        int worstIndex = -1;
        Double worstIndFitness = Double.MAX_VALUE;

        int pivotIdx = 0;
        for (Individual individual : population) {
            if (individual.getTestSolution().getObjectiveValue() < worstIndFitness) {
                worstIndFitness = individual.getTestSolution().getObjectiveValue();
                worstIndex = pivotIdx;
            }
            pivotIdx++;
        }


        return worstIndex;
    }

    private TestSolution placementHandler(TestCase tc, List<Integer> workflowOrder, String placementMethod, String order) {
        TestSolution ts = null;

        if ("MBP".equals(placementMethod)) {
            ts = workflowGreedySolver.solve(tc, workflowOrder, order);
        } else if ("GREEDY".equals(placementMethod)) {
            ts = workflowGreedySolver.solve(tc, workflowOrder, order);
        } else {
            throw new RuntimeException("Placement method is not recognized for genetic algorithm!");
        }

        return ts;
    }


    public Integer getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(Integer populationSize) {
        this.populationSize = populationSize;
    }
}
