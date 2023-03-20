package codes.erbil.vms.util;

import codes.erbil.vms.config.manager.PhysicalServerConfigManager;
import codes.erbil.vms.config.manager.VirtualMachineTypeManager;
import codes.erbil.vms.config.model.PhysicalMachine;
import codes.erbil.vms.config.model.PhysicalServerConfiguration;
import codes.erbil.vms.config.model.VirtualMachineType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ConfigFileUtil {

    @Inject
    VirtualMachineTypeManager virtualMachineTypeManager;

    @Inject
    PhysicalServerConfigManager physicalServerConfigManager;

    public List<PhysicalMachine> readPhysicalMachinesFromInputStream(InputStream inputStream) throws IOException {
        List<PhysicalMachine> pmList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(";");

                PhysicalServerConfiguration psc = physicalServerConfigManager.getPsc(splitLine[1]);
                VirtualMachineType vmType = virtualMachineTypeManager.getVm(splitLine[2]);
                Integer splitUa = Integer.valueOf(splitLine[3]);

                PhysicalMachine pm = new PhysicalMachine();
                pm.setMachineName(splitLine[0]);
                pm.setConfiguredForVmType(vmType);
                pm.setPhysicalServerConfig(psc);
                pm.setUA(splitUa);

                pmList.add(pm);
            }
        }
        return pmList;
    }
}
