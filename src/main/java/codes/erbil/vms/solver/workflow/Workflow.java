package codes.erbil.vms.solver.workflow;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Workflow {
    private List<Integer> bids = new ArrayList<>();
    private BigDecimal price = new BigDecimal(0.0);
}
