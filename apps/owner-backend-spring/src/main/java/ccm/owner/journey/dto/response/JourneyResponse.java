package ccm.owner.journey.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Response DTO for journey submission
 */
public class JourneyResponse {

    private Long id;
    private Long userId;
    private LocalDate journeyDate;
    private String startLocation;
    private String endLocation;
    private BigDecimal distanceKm;
    private BigDecimal energyUsedKwh;
    private BigDecimal estimatedCredits;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}