package codes.erbil.vms.config.model;

import lombok.Data;

@Data
public class VirtualMachineType {
    private String vmTypeId;
    private String poolName;
    private Integer baseCpuCores;
    private Integer baseRam;
    private Double reservationPrice;
}