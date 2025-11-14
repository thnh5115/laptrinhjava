package ccm.cva.report.application.dto;

import ccm.admin.journey.entity.enums.JourneyStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CarbonAuditReport(
        Long journeyId,
        Long ownerId,
        LocalDate journeyDate,
        String startLocation,
        String endLocation,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        BigDecimal creditsGenerated,
        JourneyStatus status,
        LocalDateTime createdAt,
        LocalDateTime verifiedAt,
        Long verifierId,
        String rejectionReason,
        CreditIssuanceSummary credit,
        String signature,
        Instant generatedAt
) {}
