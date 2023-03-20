package codes.erbil.vms.config.model;

import codes.erbil.vms.solver.workflow.Workflow;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TestCase {
    private String testCaseName;
    private List<PhysicalMachine> physicalMachines;
    private List<Bid> bids;
    private Integer period;
    private Map<String, List<Integer>> omega;

    // FOR EASY DATA RETRIEVAL
    private Integer availableVmCount;
    private Double bidDensity;
    private Double subbidCount;
    private Double requestedVmAlternative;
    private Double requestedVmQuantity;

    public List<Workflow> workflowList;

    public Map<String, List<Integer>> getOmega() {

        if (omega != null) {
            return omega;
        }

        omega = new HashMap<>();

        for (int a = 1; a <= physicalMachines.size(); a++) {
            PhysicalMachine pm = physicalMachines.get(a - 1);

            if (omega.containsKey(pm.getConfiguredForVmType().getVmTypeId())) {
                omega.get(pm.getConfiguredForVmType().getVmTypeId()).add(a);
            } else {
                List<Integer> idList = new ArrayList<>();
                idList.add(a);
                omega.put(pm.getConfiguredForVmType().getVmTypeId(), idList);
            }
        }

        return omega;
    }
}
