package codes.erbil.vms.config.model;

import lombok.Data;

@Data
public class SubbidCtx {
    private String vmType;
    private Integer vmSize;

    public static SubbidCtx subbidCreator(String vmType, Integer vmSize) {
        SubbidCtx sb = new SubbidCtx();
        sb.setVmType(vmType);
        sb.setVmSize(vmSize);
        return sb;
    }
}
