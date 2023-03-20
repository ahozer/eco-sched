package codes.erbil.vms.config.manager;

import codes.erbil.vms.config.model.VirtualMachineType;
import io.quarkus.runtime.Startup;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Startup
@ApplicationScoped
public class VirtualMachineTypeManager {

    private final List<VirtualMachineType> vmTypes = Collections.synchronizedList(new ArrayList<>());

    VirtualMachineTypeManager() {
        VirtualMachineType vmType1 = new VirtualMachineType();
        vmType1.setVmTypeId("v1");
        vmType1.setPoolName("VM-GP-1");
        vmType1.setBaseCpuCores(2);
        vmType1.setBaseRam(1);
        vmType1.setReservationPrice(0.041);

        VirtualMachineType vmType2 = new VirtualMachineType();
        vmType2.setVmTypeId("v2");
        vmType2.setPoolName("VM-GP-2");
        vmType2.setBaseCpuCores(2);
        vmType2.setBaseRam(2);
        vmType2.setReservationPrice(0.050);

        VirtualMachineType vmType3 = new VirtualMachineType();
        vmType3.setVmTypeId("v3");
        vmType3.setPoolName("VM-CO-1");
        vmType3.setBaseCpuCores(2);
        vmType3.setBaseRam(4);
        vmType3.setReservationPrice(0.066);

        VirtualMachineType vmType4 = new VirtualMachineType();
        vmType4.setVmTypeId("v4");
        vmType4.setPoolName("VM-CO-2");
        vmType4.setBaseCpuCores(2);
        vmType4.setBaseRam(8);
        vmType4.setReservationPrice(0.076);

        VirtualMachineType vmType5 = new VirtualMachineType();
        vmType5.setVmTypeId("v5");
        vmType5.setPoolName("VM-MO-1");
        vmType5.setBaseCpuCores(2);
        vmType5.setBaseRam(8);
        vmType5.setReservationPrice(0.100);

        VirtualMachineType vmType6 = new VirtualMachineType();
        vmType6.setVmTypeId("v6");
        vmType6.setPoolName("VM-MO-2");
        vmType6.setBaseCpuCores(2);
        vmType6.setBaseRam(10);
        vmType6.setReservationPrice(0.120);

        VirtualMachineType vmType7 = new VirtualMachineType();
        vmType7.setVmTypeId("v7");
        vmType7.setPoolName("VM-SO-1");
        vmType7.setBaseCpuCores(2);
        vmType7.setBaseRam(4);
        vmType7.setReservationPrice(0.130);

        vmTypes.add(vmType1);
        vmTypes.add(vmType2);
        vmTypes.add(vmType3);
        vmTypes.add(vmType4);
        vmTypes.add(vmType5);
        vmTypes.add(vmType6);
        vmTypes.add(vmType7);
    }

    public List<VirtualMachineType> getVmTypes() {
        return vmTypes;
    }

    public VirtualMachineType getVm(String vmTypeId) {
        if ("v1".equals(vmTypeId)) {
            return vmTypes.get(0);
        } else if ("v2".equals(vmTypeId)) {
            return vmTypes.get(1);
        } else if ("v3".equals(vmTypeId)) {
            return vmTypes.get(2);
        } else if ("v4".equals(vmTypeId)) {
            return vmTypes.get(3);
        } else if ("v5".equals(vmTypeId)) {
            return vmTypes.get(4);
        } else if ("v6".equals(vmTypeId)) {
            return vmTypes.get(5);
        } else if ("v7".equals(vmTypeId)) {
            return vmTypes.get(6);
        } else {
            throw new IllegalArgumentException("Invalid VM TYPE ID");
        }
    }
}
