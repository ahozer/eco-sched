package codes.erbil.vms.config.manager;

import codes.erbil.vms.config.model.*;
import codes.erbil.vms.entity.TestSolution;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class VerifierManager {
    public boolean isFeasible(TestCase tc, TestSolution ts) {
        return checkObjectiveValue(tc, ts) &&
                checkPhysicalMachineCapacity(tc, ts) &&
                checkDeadlineConstraints(tc, ts);
    }

    public boolean checkDeadlineConstraints(TestCase tc, TestSolution ts) {
        if (ts.getDecisionVariables() == null || ts.getDecisionVariables().isEmpty()) return true;

        for (int i = 1; i <= tc.getBids().size(); i++) {
            Bid bid = tc.getBids().get(i - 1);

            int earliest = bid.getEarliestTime();
            int latest = bid.getLatestTime();
            int duration = bid.getDuration();

            for (int t = 1; t <= tc.getPeriod(); t++) {
                if (ts.getDecisionVariables().containsKey("y[i=" + i + "][t=" + t + "]")) {
                    if (t < earliest || t + duration - 1 > latest) {
                        String errorMessage = "y[i=" + i + "][t=" + t + "] is infeasible!";

                        System.out.println(errorMessage);
                        ts.getErrors().add(errorMessage);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean checkPhysicalMachineCapacity(TestCase tc, TestSolution ts) {
        if (ts.getDecisionVariables() == null || ts.getDecisionVariables().isEmpty()) return true;

        // Physical machine a -> { Time Slot t -> {Utilization Level} }
        Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime = initializeMachineTimeMap(tc);

        boolean activeMachineCheck = true;
        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            for (int t = 1; t <= tc.getPeriod(); t++) {
                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);
                    int duration = tc.getBids().get(i - 1).getDuration();

                    for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                        Subbid subbid = bid.getSubbidList().get(j - 1);

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                            if (ts.getDecisionVariables().containsKey("z[a=" + a + "][t=" + t + "][i=" + i + "][j=" + j + "][k=" + k + "]")) {
                                Double val = ts.getDecisionVariables().get("z[a=" + a + "][t=" + t + "][i=" + i + "][j=" + j + "][k=" + k + "]");

                                Integer util = vmAlternative.getVmSize() * val.intValue();

                                // Duration kadar time slotlarını dolaş ve utilization güncelle
                                for (int t_prime = t; t_prime <= t + duration - 1; t_prime++) {
                                    Integer newUtil = utilizationOfMachineAtTime.get(a).get(t_prime) + util;

                                    utilizationOfMachineAtTime.get(a).replace(t_prime, newUtil);

                                    // Time slotta ilgili makine açık mı?
                                    if (!ts.getDecisionVariables().containsKey("o[a=" + a + "][t=" + t + "]")) {
                                        String errorMessage = "o[a=" + a + "][t=" + t + "] zamanında ilgili makine açık değil!";

                                        ts.getErrors().add(errorMessage);
                                        System.out.println(errorMessage);
                                        activeMachineCheck = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            PhysicalMachine pm = tc.getPhysicalMachines().get(a - 1);

            for (int t = 1; t <= tc.getPeriod(); t++) {
                if (utilizationOfMachineAtTime.get(a).get(t) > pm.getUA()) {
                    String errorMessage = "Physical Machine " + pm.getMachineName() + " is out of capacity at time " + t + "!";

                    System.out.println(errorMessage);
                    ts.getErrors().add(errorMessage);
                    return false;
                }
            }
        }

        return activeMachineCheck;
    }

    public Map<Integer, Map<Integer, Integer>> initializeMachineTimeMap(TestCase tc) {
        Map<Integer, Map<Integer, Integer>> utilizationOfMachineAtTime = new HashMap<>();

        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            Map<Integer, Integer> timeMap = new HashMap<>();

            utilizationOfMachineAtTime.put(a, timeMap);
            for (int t = 1; t <= tc.getPeriod(); t++) {
                timeMap.put(t, 0);
            }
        }

        return utilizationOfMachineAtTime;
    }

    public boolean checkObjectiveValue(TestCase tc, TestSolution ts) {
        double objValue = getObjValue(tc, ts);

        if (ts.getObjectiveValue() - objValue >= 0.01) {
            String errorMessage = "Objective Value verification failed!";

            System.out.println(errorMessage);
            ts.getErrors().add(errorMessage);
            return false;
        } else {
            return true;
        }
    }

    public double getObjValue(TestCase tc, TestSolution ts) {
        if (ts.getDecisionVariables() == null || ts.getDecisionVariables().isEmpty()) return 0.0;

        double totalRevenue = 0.00;
        for (int i = 1; i <= tc.getBids().size(); i++) {
            if (ts.getDecisionVariables().containsKey("x[" + i + "]")) {
                totalRevenue += tc.getBids().get(i - 1).getPrice().doubleValue();
            }
        }

        double totalIdleCost = 0.00;
        for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
            for (int t = 1; t <= tc.getPeriod(); t++) {
                Double idleCost = tc.getPhysicalMachines().get(a - 1).getPhysicalServerConfig().getIdleCost();

                if (ts.getDecisionVariables().containsKey("o[a=" + a + "][t=" + t + "]")) {
                    totalIdleCost += idleCost;
                }
            }
        }

        double totalUtilizationCost = 0.00;
        double totalReservationPrice = 0.00;
        for (int t = 1; t <= tc.getPeriod(); t++) {
            for (int a = 1; a <= tc.getPhysicalMachines().size(); a++) {
                PhysicalMachine pm = tc.getPhysicalMachines().get(a - 1);

                for (int i = 1; i <= tc.getBids().size(); i++) {
                    Bid bid = tc.getBids().get(i - 1);
                    int earliest = tc.getBids().get(i - 1).getEarliestTime();
                    int latest = tc.getBids().get(i - 1).getLatestTime();
                    int duration = tc.getBids().get(i - 1).getDuration();

                    if (t < earliest || t > latest) {
                        continue;
                    }


                    for (int j = 1; j <= bid.getSubbidList().size(); j++) {
                        Subbid subbid = bid.getSubbidList().get(j - 1);

                        for (int k = 1; k <= subbid.getVmAlternatives().size(); k++) {
                            SubbidCtx vmAlternative = subbid.getVmAlternatives().get(k - 1);

                            if (!vmAlternative.getVmType().equalsIgnoreCase(pm.getConfiguredForVmType().getVmTypeId())) {
                                continue;
                            }

                            Double e_full_minus_idle = pm.getPhysicalServerConfig().getFullLoadCost() - pm.getPhysicalServerConfig().getIdleCost();

                            for (int t_prime = Math.max(earliest, t - duration + 1); t_prime <= Math.min(latest - duration + 1, t); t_prime++) {

                                if (ts.getDecisionVariables().containsKey("z[a=" + a + "][t=" + t_prime + "][i=" + i + "][j=" + j + "][k=" + k + "]")) {
                                    Double value = ts.getDecisionVariables().get("z[a=" + a + "][t=" + t_prime + "][i=" + i + "][j=" + j + "][k=" + k + "]");

                                    // 3RD Component of the Obj. Function
                                    Double coeff3 = (e_full_minus_idle * vmAlternative.getVmSize()) / pm.getUA();
                                    totalUtilizationCost += (coeff3 * value);

                                    // 4TH Component of the Obj. Function
                                    Double p_res = pm.getConfiguredForVmType().getReservationPrice() * vmAlternative.getVmSize();
                                    totalReservationPrice += (p_res * value);
                                }
                            }
                        }
                    }
                }
            }
        }

        double objValue = totalRevenue - totalIdleCost - totalUtilizationCost - totalReservationPrice;
        return objValue;
    }
}
