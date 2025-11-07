package ccm.admin.journey.dto.response;

import ccm.admin.journey.entity.enums.JourneyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * response - Response DTO - Summary view of Journey for list display
 */

public class JourneySummaryResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private LocalDate journeyDate;
    private BigDecimal distanceKm;
    private BigDecimal creditsGenerated;
    private JourneyStatus status;
    private LocalDateTime createdAt;
}
