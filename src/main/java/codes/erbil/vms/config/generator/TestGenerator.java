package codes.erbil.vms.config.generator;

import codes.erbil.vms.config.manager.BidManager;
import codes.erbil.vms.config.manager.PhysicalMachineManager;
import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.TestCase;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TestGenerator {

    @Inject
    RandomGenerator rand;

    @ConfigProperty(name = "vms.generator.avmc")
    List<Integer> avmcSet;

    @ConfigProperty(name = "vms.generator.period")
    List<Integer> periodSet;

    @ConfigProperty(name = "vms.generator.bid.density")
    List<Double> bidDensitySet;

    @ConfigProperty(name = "vms.generator.subbid.count")
    List<Double> subbidCountSet;

    @ConfigProperty(name = "vms.generator.vm.alternative")
    List<Double> requestedVmAlternativeSet;

    @ConfigProperty(name = "vms.generator.vm.quantity")
    List<Double> requestedVmQuantitySet;

    @ConfigProperty(name = "vms.generator.vm.size")
    List<Integer> requestedVmSizeSet;

    @ConfigProperty(name = "vms.generator.deadline.probability")
    Double deadlineProbability;

    @Inject
    BidManager bidManager;

    @Inject
    PhysicalMachineManager physicalMachineManager;

    public List<TestCase> generate() {
        System.out.println("Available VM Capacities: " + avmcSet);
        System.out.println("Bid Densities: " + bidDensitySet);
        System.out.println("Periods: " + periodSet);
        System.out.println("Bid Densities: " + bidDensitySet);
        System.out.println("Subbid Counts (Poisson): " + subbidCountSet);
        System.out.println("VM Alternatives in Subbid (Poisson): " + requestedVmAlternativeSet);
        System.out.println("VM Quantities (Poisson): " + requestedVmQuantitySet);
        System.out.println("VM Sizes: " + requestedVmSizeSet);
        System.out.println("Deadline Probability: " + deadlineProbability);

        List<TestCase> tcList = new ArrayList<>();

        for (Integer availableVmCount : avmcSet) {
            for (Integer period : periodSet) {
                for (Double bidDensity : bidDensitySet) {
                    for (Double subbidCount : subbidCountSet) {
                        for (Double requestedVmAlternative : requestedVmAlternativeSet) {
                            for (Double requestedVmQuantity : requestedVmQuantitySet) {

                                StringBuilder sb = new StringBuilder();
                                sb.append("AVMC" + availableVmCount + "_");
                                sb.append("T" + period + "_");
                                sb.append("BD" + bidDensity + "_");
                                sb.append("SBC" + subbidCount + "_");
                                sb.append("A" + requestedVmAlternative + "_");
                                sb.append("Q" + requestedVmQuantity);

                                List<Bid> generatedBids = bidManager.generateBids(availableVmCount,
                                        period,
                                        bidDensity,
                                        subbidCount,
                                        requestedVmAlternative,
                                        requestedVmQuantity,
                                        requestedVmSizeSet,
                                        deadlineProbability);

                                TestCase tc = new TestCase();
                                tc.setTestCaseName(sb.toString());
                                tc.setBids(generatedBids);
                                tc.setPeriod(period);
                                tc.setPhysicalMachines(physicalMachineManager.getPhysicalMachineMap().get(availableVmCount));

                                tc.setBidDensity(bidDensity);
                                tc.setAvailableVmCount(availableVmCount);
                                tc.setSubbidCount(subbidCount);
                                tc.setRequestedVmAlternative(requestedVmAlternative);
                                tc.setRequestedVmQuantity(requestedVmQuantity);

                                tcList.add(tc);
                            }
                        }

                    }
                }
            }
        }
        return tcList;
    }
}
