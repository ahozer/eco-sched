package codes.erbil.vms.config.model;

import lombok.Data;

@Data
public class PhysicalMachine {
    private String machineName;
    private PhysicalServerConfiguration physicalServerConfig;
    private VirtualMachineType configuredForVmType;
    private Integer uA;
}