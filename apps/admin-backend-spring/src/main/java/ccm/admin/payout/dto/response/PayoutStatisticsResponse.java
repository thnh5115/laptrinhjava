package ccm.admin.payout.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutStatisticsResponse {
    private Long totalPayouts;
    private Long pendingPayouts;
    private Long approvedPayouts;
    private Long rejectedPayouts;
    private Long completedPayouts;
    private BigDecimal totalAmountRequested;
    private BigDecimal totalAmountApproved;
    private BigDecimal totalAmountCompleted;
    private Double approvalRate;  // (approved / total) * 100
}
