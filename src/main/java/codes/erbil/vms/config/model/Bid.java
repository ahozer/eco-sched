package codes.erbil.vms.config.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Bid {
    private List<Subbid> subbidList;
    private Integer earliestTime;
    private Integer latestTime;
    private Integer duration;
    private BigDecimal price;

    private List<Integer> dependencyList;
}
