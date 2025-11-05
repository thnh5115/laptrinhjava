package ccm.buyer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyerDashboardResponse {
    private long totalOrders;
    private double totalAmount;
    private double totalSpent;
    private long pendingOrders;
    private long paidOrders;
    private long cancelledOrders;
    private long completedOrders;
    private long approvedOrders;
    private long rejectedOrders;
}
