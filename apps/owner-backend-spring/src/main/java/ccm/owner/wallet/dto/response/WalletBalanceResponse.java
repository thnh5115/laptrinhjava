package ccm.owner.wallet.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Response DTO for wallet balance inquiry
 */
public class WalletBalanceResponse {

    private Long walletId;
    private Long userId;
    private String userEmail;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime lastUpdated;

    // Additional information
    private BigDecimal totalCreditsGenerated;
    private BigDecimal totalEarnings;
    private BigDecimal totalWithdrawals;
    private BigDecimal pendingWithdrawals;
}