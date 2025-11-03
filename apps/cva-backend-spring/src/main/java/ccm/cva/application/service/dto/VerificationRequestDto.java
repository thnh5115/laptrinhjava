package ccm.cva.application.service.dto;

import ccm.cva.domain.model.enums.VerificationRequestStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VerificationRequestDto(
        UUID id,
        UUID ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        VerificationRequestStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime verifiedAt,
        UUID verifierId,
        String notes
) {}
