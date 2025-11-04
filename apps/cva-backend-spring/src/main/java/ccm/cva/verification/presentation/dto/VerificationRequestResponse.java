package ccm.cva.verification.presentation.dto;

import ccm.cva.verification.domain.VerificationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VerificationRequestResponse(
        UUID id,
        UUID ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        VerificationStatus status,
        Instant createdAt,
        Instant verifiedAt,
        UUID verifierId,
        String notes
) {}
