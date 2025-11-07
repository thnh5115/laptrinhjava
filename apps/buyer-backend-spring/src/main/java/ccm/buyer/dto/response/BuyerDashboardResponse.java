package ccm.buyer.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class BuyerDashboardResponse {
  private long totalOrders;
  private long pendingTransactions;
  private long completedTransactions;
  private double totalSpent;
}
