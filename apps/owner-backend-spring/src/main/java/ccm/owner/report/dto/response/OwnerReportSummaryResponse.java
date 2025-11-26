package ccm.owner.report.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerReportSummaryResponse {
    // Journey statistics
    private Long totalJourneys;
    private Long verifiedJourneys;
    private Long pendingJourneys;
    private Long rejectedJourneys;

    // Credit statistics
    private BigDecimal totalCreditsGenerated;
    private Double averageCreditsPerJourney;
    private Double verificationRate;

    // Financial statistics
    private BigDecimal totalEarnings;
    private BigDecimal totalWithdrawals;
    private BigDecimal pendingWithdrawals;
    private BigDecimal availableBalance;
}

