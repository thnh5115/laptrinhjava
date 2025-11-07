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
/** response - Response DTO - Detailed Journey information */

public class JourneyDetailResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private LocalDate journeyDate;
    private String startLocation;
    private String endLocation;
    private BigDecimal distanceKm;
    private BigDecimal energyUsedKwh;
    private BigDecimal creditsGenerated;
    private JourneyStatus status;
    private Long verifiedBy;
    private String verifiedByEmail;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
