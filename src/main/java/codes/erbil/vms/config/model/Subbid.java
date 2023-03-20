package codes.erbil.vms.config.model;

import lombok.Data;

import java.util.List;

@Data
public class Subbid {
    private List<SubbidCtx> vmAlternatives;
    private Integer quantity;
}
