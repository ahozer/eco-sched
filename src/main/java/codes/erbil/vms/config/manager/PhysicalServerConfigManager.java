package codes.erbil.vms.config.manager;

import codes.erbil.vms.config.model.PhysicalServerConfiguration;
import io.quarkus.runtime.Startup;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

@Startup
@ApplicationScoped
public class PhysicalServerConfigManager {

    private final List<PhysicalServerConfiguration> pscList = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Map<Integer, Integer>> uAQuantityMap;

    PhysicalServerConfigManager() {
        PhysicalServerConfiguration prc1 = new PhysicalServerConfiguration();
        prc1.setPoolName("PS-GP-1");
        prc1.setNumberOfCores(64);
        prc1.setMemoryAmount(64);
        prc1.setIdlePower(55.0);
        prc1.setIdleCost(0.2376);
        prc1.setFullLoadPower(231.0);
        prc1.setFullLoadCost(0.9979);
        prc1.setConfiguredForVm("v1");
        prc1.setUA(48);
        pscList.add(prc1);

        PhysicalServerConfiguration prc2 = new PhysicalServerConfiguration();
        prc2.setPoolName("PS-GP-2");
        prc2.setNumberOfCores(128);
        prc2.setMemoryAmount(256);
        prc2.setIdlePower(106.0);
        prc2.setIdleCost(0.4579);
        prc2.setFullLoadPower(430.0);
        prc2.setFullLoadCost(1.8576);
        prc2.setConfiguredForVm("v2");
        prc2.setUA(96);
        pscList.add(prc2);

        PhysicalServerConfiguration prc3 = new PhysicalServerConfiguration();
        prc3.setPoolName("PS-CO-1");
        prc3.setNumberOfCores(112);
        prc3.setMemoryAmount(384);
        prc3.setIdlePower(138.0);
        prc3.setIdleCost(0.5962);
        prc3.setFullLoadPower(721.0);
        prc3.setFullLoadCost(3.1147);
        prc3.setConfiguredForVm("v3");
        prc3.setUA(96);
        pscList.add(prc3);

        PhysicalServerConfiguration prc4 = new PhysicalServerConfiguration();
        prc4.setPoolName("PS-CO-2");
        prc4.setNumberOfCores(112);
        prc4.setMemoryAmount(768);
        prc4.setIdlePower(258.0);
        prc4.setIdleCost(1.1146);
        prc4.setFullLoadPower(1117.0);
        prc4.setFullLoadCost(4.8254);
        prc4.setConfiguredForVm("v4");
        prc4.setUA(96);
        pscList.add(prc4);

        PhysicalServerConfiguration prc5 = new PhysicalServerConfiguration();
        prc5.setPoolName("PS-MO-1");
        prc5.setNumberOfCores(64);
        prc5.setMemoryAmount(512);
        prc5.setIdlePower(156.0);
        prc5.setIdleCost(0.6739);
        prc5.setFullLoadPower(680.0);
        prc5.setFullLoadCost(2.9376);
        prc5.setConfiguredForVm("v5");
        prc5.setUA(48);
        pscList.add(prc5);

        PhysicalServerConfiguration prc6 = new PhysicalServerConfiguration();
        prc6.setPoolName("PS-MO-2");
        prc6.setNumberOfCores(80);
        prc6.setMemoryAmount(512);
        prc6.setIdlePower(118.0);
        prc6.setIdleCost(0.5098);
        prc6.setFullLoadPower(633.0);
        prc6.setFullLoadCost(2.7346);
        prc6.setConfiguredForVm("v6");
        prc6.setUA(64);
        pscList.add(prc6);

        PhysicalServerConfiguration prc7 = new PhysicalServerConfiguration();
        prc7.setPoolName("PS-SO-1");
        prc7.setNumberOfCores(80);
        prc7.setMemoryAmount(256);
        prc7.setIdlePower(137.0);
        prc7.setIdleCost(0.5918);
        prc7.setFullLoadPower(550.0);
        prc7.setFullLoadCost(2.376);
        prc7.setConfiguredForVm("v7");
        prc7.setUA(64);
        pscList.add(prc7);

        // Fill uAQuantityMap
        uAQuantityMap = new HashMap<>();

        Map<Integer, Integer> v1Map = new HashMap<>();
        v1Map.put(8, 6);
        v1Map.put(16, 3);
        v1Map.put(24, 2);
        v1Map.put(32, 1);
        v1Map.put(48, 1);
        uAQuantityMap.put("v1", v1Map);

        Map<Integer, Integer> v2Map = new HashMap<>();
        v2Map.put(8, 12);
        v2Map.put(16, 6);
        v2Map.put(24, 4);
        v2Map.put(32, 3);
        v2Map.put(48, 2);
        uAQuantityMap.put("v2", v2Map);

        Map<Integer, Integer> v3Map = new HashMap<>();
        v3Map.put(8, 12);
        v3Map.put(16, 6);
        v3Map.put(24, 4);
        v3Map.put(32, 3);
        v3Map.put(48, 2);
        uAQuantityMap.put("v3", v3Map);

        Map<Integer, Integer> v4Map = new HashMap<>();
        v4Map.put(8, 12);
        v4Map.put(16, 6);
        v4Map.put(24, 4);
        v4Map.put(32, 3);
        v4Map.put(48, 2);
        uAQuantityMap.put("v4", v4Map);


        Map<Integer, Integer> v5Map = new HashMap<>();
        v5Map.put(8, 6);
        v5Map.put(16, 3);
        v5Map.put(24, 2);
        v5Map.put(32, 1);
        v5Map.put(48, 1);
        uAQuantityMap.put("v5", v5Map);

        Map<Integer, Integer> v6Map = new HashMap<>();
        v6Map.put(8, 8);
        v6Map.put(16, 4);
        v6Map.put(24, 2);
        v6Map.put(32, 2);
        v6Map.put(48, 1);
        uAQuantityMap.put("v6", v6Map);

        Map<Integer, Integer> v7Map = new HashMap<>();
        v7Map.put(8, 8);
        v7Map.put(16, 4);
        v7Map.put(24, 2);
        v7Map.put(32, 2);
        v7Map.put(48, 1);
        uAQuantityMap.put("v7", v7Map);

    }

    public List<PhysicalServerConfiguration> getPscList() {
        return pscList;
    }

    public Map<String, Map<Integer, Integer>> getuAQuantityMap() {
        return uAQuantityMap;
    }

    public PhysicalServerConfiguration getPsc(String poolName) {
        if ("PS-GP-1".equals(poolName)) {
            return pscList.get(0);
        } else if ("PS-GP-2".equals(poolName)) {
            return pscList.get(1);
        } else if ("PS-CO-1".equals(poolName)) {
            return pscList.get(2);
        } else if ("PS-CO-2".equals(poolName)) {
            return pscList.get(3);
        } else if ("PS-MO-1".equals(poolName)) {
            return pscList.get(4);
        } else if ("PS-MO-2".equals(poolName)) {
            return pscList.get(5);
        } else if ("PS-SO-1".equals(poolName)) {
            return pscList.get(6);
        } else {
            throw new IllegalArgumentException("Invalid POOL Name for Physical Machine Configuration");
        }
    }

    public PhysicalServerConfiguration thetaFunction(String vmType) {
        if ("v1".equals(vmType)) {
            return pscList.get(0);
        } else if ("v2".equals(vmType)) {
            return pscList.get(1);
        } else if ("v3".equals(vmType)) {
            return pscList.get(2);
        } else if ("v4".equals(vmType)) {
            return pscList.get(3);
        } else if ("v5".equals(vmType)) {
            return pscList.get(4);
        } else if ("v6".equals(vmType)) {
            return pscList.get(5);
        } else if ("v7".equals(vmType)) {
            return pscList.get(6);
        } else {
            throw new IllegalArgumentException("Invalid VM TYPE ID");
        }
    }
}
