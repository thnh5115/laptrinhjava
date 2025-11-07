package ccm.admin.journey.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** response - Response DTO - Journey statistics for CO2 impact dashboard */

public class JourneyStatisticsResponse {

    /** Total number of journeys */
    private Long totalJourneys;

    /** Number of pending journeys */
    private Long pendingJourneys;

    /** Number of verified journeys */
    private Long verifiedJourneys;

    /** Number of rejected journeys */
    private Long rejectedJourneys;

    /** Total carbon credits generated from verified journeys */
    private BigDecimal totalCreditsGenerated;

    /** Verification rate (verified / total * 100) */
    private Double verificationRate;
}
