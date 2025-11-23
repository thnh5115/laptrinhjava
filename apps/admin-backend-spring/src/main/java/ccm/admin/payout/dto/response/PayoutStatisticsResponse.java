package ccm.admin.payout.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutStatisticsResponse {
    private Long totalCount;
    private Long pendingCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long completedCount;
    private BigDecimal totalAmount;
    private BigDecimal pendingAmount;
    private BigDecimal approvedAmount;
    private BigDecimal rejectedAmount;
    private BigDecimal completedAmount;
    private Double approvalRate;  // (approved+completed / total) * 100
}
