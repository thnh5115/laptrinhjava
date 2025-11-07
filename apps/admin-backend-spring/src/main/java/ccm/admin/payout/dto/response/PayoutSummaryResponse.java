package ccm.admin.payout.dto.response;

import ccm.admin.payout.entity.enums.PayoutStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutSummaryResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private PayoutStatus status;
    private String paymentMethod;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}
