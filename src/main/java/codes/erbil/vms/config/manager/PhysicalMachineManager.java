package codes.erbil.vms.config.manager;

import codes.erbil.vms.config.model.PhysicalMachine;
import codes.erbil.vms.util.ConfigFileUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PhysicalMachineManager {

    @Inject
    ConfigFileUtil configFileUtil;

    private Map<Integer, List<PhysicalMachine>> physicalMachineMap;

    public Map<Integer, List<PhysicalMachine>> getPhysicalMachineMap() {
        if (physicalMachineMap == null) {
            physicalMachineMap = new HashMap<>();

            loadPhysicalMachines();
        }
        return physicalMachineMap;
    }

    private void loadPhysicalMachines() {
        Class clazz = PhysicalMachineManager.class;

        InputStream inputStreamPm_512 = clazz.getResourceAsStream("/pm_512.txt");
        InputStream inputStreamPm_1024 = clazz.getResourceAsStream("/pm_1024.txt");
        InputStream inputStreamPm_1536 = clazz.getResourceAsStream("/pm_1536.txt");
        InputStream inputStreamPm_2048 = clazz.getResourceAsStream("/pm_2048.txt");
        InputStream inputStreamPm_3072 = clazz.getResourceAsStream("/pm_3072.txt");
        InputStream inputStreamPm_4096 = clazz.getResourceAsStream("/pm_4096.txt");

        List<PhysicalMachine> pm512 = new ArrayList<>();
        List<PhysicalMachine> pm1024 = new ArrayList<>();
        List<PhysicalMachine> pm1536 = new ArrayList<>();
        List<PhysicalMachine> pm2048 = new ArrayList<>();
        List<PhysicalMachine> pm3072 = new ArrayList<>();
        List<PhysicalMachine> pm4096 = new ArrayList<>();
        try {
            pm512 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_512);
            pm1024 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_1024);
            pm1536 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_1536);
            pm2048 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_2048);
            pm3072 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_3072);
            pm4096 = configFileUtil.readPhysicalMachinesFromInputStream(inputStreamPm_4096);
        } catch (IOException e) {
            e.printStackTrace();
        }

        physicalMachineMap.put(512, pm512);
        physicalMachineMap.put(1024, pm1024);
        physicalMachineMap.put(1536, pm1536);
        physicalMachineMap.put(2048, pm2048);
        physicalMachineMap.put(3072, pm3072);
        physicalMachineMap.put(4096, pm4096);
    }
}
