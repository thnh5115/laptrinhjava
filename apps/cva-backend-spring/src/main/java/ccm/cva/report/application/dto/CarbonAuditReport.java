package ccm.cva.report.application.dto;

import ccm.cva.verification.domain.VerificationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CarbonAuditReport(
        UUID requestId,
        UUID ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        VerificationStatus status,
        Instant createdAt,
        Instant verifiedAt,
        UUID verifierId,
        String notes,
        CreditIssuanceSummary issuance,
        String signature,
        Instant generatedAt
) {}
