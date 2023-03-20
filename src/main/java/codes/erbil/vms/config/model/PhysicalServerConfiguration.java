package codes.erbil.vms.config.model;

import lombok.Data;

@Data
public class PhysicalServerConfiguration {
    private String poolName;
    private Integer numberOfCores;
    private Integer memoryAmount;
    private Double idlePower;
    private Double idleCost;
    private Double fullLoadPower;
    private Double fullLoadCost;
    private String configuredForVm;
    private Integer uA;
}
