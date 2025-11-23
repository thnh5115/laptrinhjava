package ccm.cva.verification.presentation.dto;

import ccm.cva.verification.domain.VerificationStatus;
import java.math.BigDecimal;

import java.time.LocalDateTime;


public record VerificationRequestResponse(
        Long id,
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
        CreditIssuanceResponse creditIssuance
) {}
