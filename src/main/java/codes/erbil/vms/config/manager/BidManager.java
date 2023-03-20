package codes.erbil.vms.config.manager;

import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.PhysicalServerConfiguration;
import codes.erbil.vms.config.model.Subbid;
import codes.erbil.vms.config.model.SubbidCtx;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@ApplicationScoped
public class BidManager {

    @Inject
    RandomGenerator rand;

    @Inject
    VirtualMachineTypeManager virtualMachineTypeManager;

    @Inject
    PhysicalServerConfigManager physicalServerConfigManager;

    public List<Bid> generateBids(Integer availableVmCount,
                                  Integer period,
                                  Double bidDensity,
                                  Double subbidCount,
                                  Double requestedVmAlternative,
                                  Double requestedVmQuantity,
                                  List<Integer> requestedVmSizeSet,
                                  Double deadlineProbability) {

        Double priceCoeff = 1.25;
        List<Bid> bids = new ArrayList<>();

        PoissonDistribution subbidCountDistribution = new PoissonDistribution(rand, subbidCount, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        PoissonDistribution requestedVmAlternativeDistribution = new PoissonDistribution(rand, requestedVmAlternative, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        PoissonDistribution requestedVmQuantityDistribution = new PoissonDistribution(rand, requestedVmQuantity, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);

        UniformIntegerDistribution vmAlternativeSelectionDistribution = new UniformIntegerDistribution(rand, 1, 7);

        UniformIntegerDistribution vmSizeDistribution = new UniformIntegerDistribution(rand, 0, requestedVmSizeSet.size() - 1);
        UniformIntegerDistribution durationDistribution = new UniformIntegerDistribution(rand, 1, period / 2);

        int avmcTimesPeriod = (availableVmCount * period);

        double currentBidDensity = 0.00;
        int currentVmSize = 0;
        while (currentBidDensity < bidDensity) {
            Bid bid = new Bid();
            List<Subbid> subbids = new ArrayList<>();

            int subbidCountSample = subbidCountDistribution.sample();
            while (subbidCountSample < 1) {
                subbidCountSample = subbidCountDistribution.sample();
            }

            // CREATE SUBBIDS
            for (int j = 0; j < subbidCountSample; j++) {

                int vmAlternativeSample = requestedVmAlternativeDistribution.sample();
                /*
                7 den büyük istemememizin sebebi 7 tip VM olması.
                Aynı VM tipinden bir subbid içerisinde OR'lama yapılamaz
                 */
                while (vmAlternativeSample < 1 || vmAlternativeSample > 7) {
                    vmAlternativeSample = requestedVmAlternativeDistribution.sample();
                }

                // CREATE VM ALTERNATIVES
                // Select VM Size
                Integer vmSizeIndex = vmSizeDistribution.sample();

                //Ordered Map for VM alternatives
                Map<Integer, String> vmTypeMap = new TreeMap<>();

                // FIRST PASS FOR CREATING ALTERNATIVES
                for (int k = 1; k <= vmAlternativeSample; k++) {
                    // Select VM Type as an alternative (use Set for avoiding duplicate vm types)
                    Integer vmTypeIndex = vmAlternativeSelectionDistribution.sample();
                    while (vmTypeMap.containsKey(vmTypeIndex)) {
                        vmTypeIndex = vmAlternativeSelectionDistribution.sample();
                    }
                    vmTypeMap.put(vmTypeIndex, "v" + vmTypeIndex);
                }

                // SECOND PASS FOR CREATING SUBBIDCTX OBJECTS
                List<SubbidCtx> alternativeList = vmTypeMap.values().stream()
                        .map(e -> SubbidCtx.subbidCreator(e, requestedVmSizeSet.get(vmSizeIndex)))
                        .collect(Collectors.toList());

                // SELECT QUANTITY FOR SUBBID
                Integer vmQuantitySample = requestedVmQuantityDistribution.sample();
                while (vmQuantitySample < 1) {
                    vmQuantitySample = requestedVmQuantityDistribution.sample();
                }

                // CREATE SUBBID
                Subbid subbid = new Subbid();
                subbid.setQuantity(vmQuantitySample);
                subbid.setVmAlternatives(alternativeList);
                subbids.add(subbid);
            }

            // SET DURATION/REQUESTED NUMBER OF TIME SLOTS
            int duration = durationDistribution.sample();

            // SET EARLIEST AND LATEST TIME SLOTS
            int earliestTime = 1;
            int latestTime = period;

            // Earliest -> uniform 1 , T-d_i+1 | Latest -> uniform e_i+d_i-1 , T
            if (rand.nextDouble() < deadlineProbability) {
                UniformIntegerDistribution earliestDistribution = new UniformIntegerDistribution(rand, 1, period - duration + 1);
                earliestTime = earliestDistribution.sample();

                UniformIntegerDistribution latestDistribution = new UniformIntegerDistribution(rand, earliestTime + duration - 1, period);
                latestTime = latestDistribution.sample();
            }

            // SET PRICE
            double totalPriceOfSubbids = 0.00;
            int bidTotalSizeOfVms = 0;
            for (Subbid s : subbids) {
                // Get last (most expensive) alternative (Sorted)
                SubbidCtx sbc = s.getVmAlternatives().get(s.getVmAlternatives().size() - 1);

                // FOR BID DENSITY CALCULATION, COUNT ALL VM SLOTS
                Integer sizeTimesQuantity = sbc.getVmSize() * s.getQuantity();
                bidTotalSizeOfVms += sizeTimesQuantity;

                double reservationCost = virtualMachineTypeManager.getVm(sbc.getVmType()).getReservationPrice() * sizeTimesQuantity;

                // GET PHYSICAL SERVER CONFIGURATION FOR THIS VM TYPE
                PhysicalServerConfiguration psc = physicalServerConfigManager.thetaFunction(sbc.getVmType());

                int uaQuantity = physicalServerConfigManager.getuAQuantityMap().get(sbc.getVmType()).get(sbc.getVmSize());

                double requiredNumOfPms = Math.ceil(s.getQuantity() / ((double) uaQuantity));
                double totalIdleCost = requiredNumOfPms * psc.getIdleCost();

                int remainingQuantity = s.getQuantity();
                double runningMachineCost = 0.0;
                for (int ppm = 1; ppm <= requiredNumOfPms; ppm++) {

                    if (remainingQuantity < uaQuantity) {
                        runningMachineCost += ((psc.getFullLoadCost() - psc.getIdleCost()) / psc.getUA()) * (remainingQuantity * sbc.getVmSize());
                    } else {
                        runningMachineCost += ((psc.getFullLoadCost() - psc.getIdleCost()) / psc.getUA()) * (uaQuantity * sbc.getVmSize());
                    }

                    remainingQuantity = remainingQuantity - uaQuantity;
                }

                // RESERVATION PRICE + IDLECOST + # OF FULL MACHINES ENERGY COST + # OF NOT FULL MACHINES ENERGY COST
                double energyCost = totalIdleCost + runningMachineCost;
                totalPriceOfSubbids = totalPriceOfSubbids + (reservationCost + energyCost);
            }

            // For bid density calculation
            bidTotalSizeOfVms = bidTotalSizeOfVms * duration;

            totalPriceOfSubbids = totalPriceOfSubbids * duration;

            NormalDistribution priceDistribution = new NormalDistribution(rand, totalPriceOfSubbids, totalPriceOfSubbids / 2.0);
            Double price = priceDistribution.sample();
            while (price < totalPriceOfSubbids) {
                price = priceDistribution.sample();
            }

            // CREATE BID
            bid.setSubbidList(subbids);
            bid.setEarliestTime(earliestTime);
            bid.setLatestTime(latestTime);
            bid.setDuration(duration);
            bid.setPrice(BigDecimal.valueOf(priceCoeff * price));

            currentVmSize += bidTotalSizeOfVms;

            currentBidDensity = (double) currentVmSize / avmcTimesPeriod;

            System.out.println("Current Bid Density:" + currentBidDensity);
            bids.add(bid);
        }

        return bids;
    }

    public List<Bid> generateToyTestCase1() {
        List<Bid> toyTestCase1Bids = Collections.synchronizedList(new ArrayList<>());

        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v1");
        sbc11_1.setVmSize(24);

        SubbidCtx sbc11_2 = new SubbidCtx();
        sbc11_2.setVmType("v2");
        sbc11_2.setVmSize(24);

        SubbidCtx sbc12_1 = new SubbidCtx();
        sbc12_1.setVmType("v7");
        sbc12_1.setVmSize(32);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1, sbc11_2));
        sb11.setQuantity(1);

        Subbid sb12 = new Subbid();
        sb12.setVmAlternatives(Arrays.asList(sbc12_1));
        sb12.setQuantity(2);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11, sb12));
        b1.setEarliestTime(1);
        b1.setLatestTime(10);
        b1.setDuration(5);
        b1.setPrice(BigDecimal.valueOf(65.0));

        // Bid #2
        SubbidCtx sbc21_1 = new SubbidCtx();
        sbc21_1.setVmType("v2");
        sbc21_1.setVmSize(48);

        SubbidCtx sbc22_1 = new SubbidCtx();
        sbc22_1.setVmType("v3");
        sbc22_1.setVmSize(32);

        Subbid sb21 = new Subbid();
        sb21.setVmAlternatives(Arrays.asList(sbc21_1));
        sb21.setQuantity(2);

        Subbid sb22 = new Subbid();
        sb22.setVmAlternatives(Arrays.asList(sbc22_1));
        sb22.setQuantity(3);

        Bid b2 = new Bid();
        b2.setSubbidList(Arrays.asList(sb21, sb22));
        b2.setEarliestTime(1);
        b2.setLatestTime(10);
        b2.setDuration(5);
        b2.setPrice(BigDecimal.valueOf(100.0));

        // Bid #3
        SubbidCtx sbc31_1 = new SubbidCtx();
        sbc31_1.setVmType("v3");
        sbc31_1.setVmSize(48);

        SubbidCtx sbc31_2 = new SubbidCtx();
        sbc31_2.setVmType("v4");
        sbc31_2.setVmSize(48);

        Subbid sb31 = new Subbid();
        sb31.setVmAlternatives(Arrays.asList(sbc31_1, sbc31_2));
        sb31.setQuantity(2);

        SubbidCtx sbc32_1 = new SubbidCtx();
        sbc32_1.setVmType("v6");
        sbc32_1.setVmSize(32);

        SubbidCtx sbc32_2 = new SubbidCtx();
        sbc32_2.setVmType("v7");
        sbc32_2.setVmSize(32);

        Subbid sb32 = new Subbid();
        sb32.setVmAlternatives(Arrays.asList(sbc32_1, sbc32_2));
        sb32.setQuantity(2);

        Bid b3 = new Bid();
        b3.setSubbidList(Arrays.asList(sb31, sb32));
        b3.setEarliestTime(1);
        b3.setLatestTime(7);
        b3.setDuration(6);
        b3.setPrice(BigDecimal.valueOf(150.0));

        // Bid #4
        SubbidCtx sbc41_1 = new SubbidCtx();
        sbc41_1.setVmType("v5");
        sbc41_1.setVmSize(48);

        Subbid sb41 = new Subbid();
        sb41.setVmAlternatives(Arrays.asList(sbc41_1));
        sb41.setQuantity(1);

        SubbidCtx sbc42_1 = new SubbidCtx();
        sbc42_1.setVmType("v4");
        sbc42_1.setVmSize(48);

        Subbid sb42 = new Subbid();
        sb42.setVmAlternatives(Arrays.asList(sbc42_1));
        sb42.setQuantity(2);

        Bid b4 = new Bid();
        b4.setSubbidList(Arrays.asList(sb41, sb42));
        b4.setEarliestTime(1);
        b4.setLatestTime(10);
        b4.setDuration(2);
        b4.setPrice(BigDecimal.valueOf(50.0));

        // Bid #5
        SubbidCtx sbc51_1 = new SubbidCtx();
        sbc51_1.setVmType("v2");
        sbc51_1.setVmSize(48);

        SubbidCtx sbc52_1 = new SubbidCtx();
        sbc52_1.setVmType("v3");
        sbc52_1.setVmSize(32);

        Subbid sb51 = new Subbid();
        sb51.setVmAlternatives(Arrays.asList(sbc51_1));
        sb51.setQuantity(2);

        Subbid sb52 = new Subbid();
        sb52.setVmAlternatives(Arrays.asList(sbc52_1));
        sb52.setQuantity(3);

        Bid b5 = new Bid();
        b5.setSubbidList(Arrays.asList(sb51, sb52));
        b5.setEarliestTime(1);
        b5.setLatestTime(10);
        b5.setDuration(5);
        b5.setPrice(BigDecimal.valueOf(80.0));

        toyTestCase1Bids.add(b1);
        toyTestCase1Bids.add(b2);
        toyTestCase1Bids.add(b3);
        toyTestCase1Bids.add(b4);
        toyTestCase1Bids.add(b5);

        return toyTestCase1Bids;
    }

    public List<Bid> generateToyTestCase2() {
        List<Bid> toyTestCase2Bids = Collections.synchronizedList(new ArrayList<>());

        SubbidCtx sbc11_1 = new SubbidCtx();
        sbc11_1.setVmType("v1");
        sbc11_1.setVmSize(32);

        SubbidCtx sbc11_2 = new SubbidCtx();
        sbc11_2.setVmType("v5");
        sbc11_2.setVmSize(32);

        Subbid sb11 = new Subbid();
        sb11.setVmAlternatives(Arrays.asList(sbc11_1, sbc11_2));
        sb11.setQuantity(3);

        Bid b1 = new Bid();
        b1.setSubbidList(Arrays.asList(sb11));
        b1.setEarliestTime(1);
        b1.setLatestTime(10);
        b1.setDuration(7);
        b1.setPrice(BigDecimal.valueOf(200));

        SubbidCtx sbc21_1 = new SubbidCtx();
        sbc21_1.setVmType("v2");
        sbc21_1.setVmSize(32);

        Subbid sb21 = new Subbid();
        sb21.setVmAlternatives(Arrays.asList(sbc21_1));
        sb21.setQuantity(2);

        Bid b2 = new Bid();
        b2.setSubbidList(Arrays.asList(sb21));
        b2.setEarliestTime(1);
        b2.setLatestTime(10);
        b2.setDuration(3);
        b2.setPrice(BigDecimal.valueOf(50));

        SubbidCtx sbc31_1 = new SubbidCtx();
        sbc31_1.setVmType("v2");
        sbc31_1.setVmSize(32);

        Subbid sb31 = new Subbid();
        sb31.setVmAlternatives(Arrays.asList(sbc31_1));
        sb31.setQuantity(1);

        Bid b3 = new Bid();
        b3.setSubbidList(Arrays.asList(sb31));
        b3.setEarliestTime(1);
        b3.setLatestTime(10);
        b3.setDuration(3);
        b3.setPrice(BigDecimal.valueOf(30));

        SubbidCtx sbc41_1 = new SubbidCtx();
        sbc41_1.setVmType("v2");
        sbc41_1.setVmSize(32);

        Subbid sb41 = new Subbid();
        sb41.setVmAlternatives(Arrays.asList(sbc41_1));
        sb41.setQuantity(3);

        Bid b4 = new Bid();
        b4.setSubbidList(Arrays.asList(sb41));
        b4.setEarliestTime(1);
        b4.setLatestTime(10);
        b4.setDuration(7);
        b4.setPrice(BigDecimal.valueOf(60));

        SubbidCtx sbc51_1 = new SubbidCtx();
        sbc51_1.setVmType("v1");
        sbc51_1.setVmSize(24);

        SubbidCtx sbc51_2 = new SubbidCtx();
        sbc51_2.setVmType("v5");
        sbc51_2.setVmSize(24);

        Subbid sb51 = new Subbid();
        sb51.setVmAlternatives(Arrays.asList(sbc51_1, sbc51_2));
        sb51.setQuantity(4);

        Bid b5 = new Bid();
        b5.setSubbidList(Arrays.asList(sb51));
        b5.setEarliestTime(1);
        b5.setLatestTime(10);
        b5.setDuration(7);
        b5.setPrice(BigDecimal.valueOf(200));

        toyTestCase2Bids.add(b1);
        toyTestCase2Bids.add(b2);
        toyTestCase2Bids.add(b3);
        toyTestCase2Bids.add(b4);
        toyTestCase2Bids.add(b5);
        return toyTestCase2Bids;
    }
}
