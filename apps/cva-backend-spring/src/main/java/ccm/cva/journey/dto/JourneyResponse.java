package ccm.cva.journey.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record JourneyResponse(
        Long id,
        Long ownerId,
        LocalDate journeyDate,
        String startLocation,
        String endLocation,
        BigDecimal distanceKm,
        BigDecimal energyUsedKwh,
        BigDecimal creditsGenerated,
        JourneyDecisionStatus status,
        Long verifiedBy,
        LocalDateTime verifiedAt,
        String rejectionReason,
        LocalDateTime createdAt
) {}
