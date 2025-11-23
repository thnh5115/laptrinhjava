package ccm.cva.report.application.dto;

import ccm.cva.verification.domain.VerificationStatus;
import java.math.BigDecimal;

import java.time.LocalDateTime;


public record CarbonAuditReport(
        Long requestId,
        Long ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        VerificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime verifiedAt,
        Long verifierId,
        String notes,
        CreditIssuanceSummary issuance,
        String signature,
        LocalDateTime generatedAt
) {}
