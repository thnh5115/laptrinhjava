package ccm.buyer.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class BuyerDashboardResponse {
    private long totalOrders;
    private long pendingTransactions;
    private long completedTransactions;
    private BigDecimal totalSpent;
}