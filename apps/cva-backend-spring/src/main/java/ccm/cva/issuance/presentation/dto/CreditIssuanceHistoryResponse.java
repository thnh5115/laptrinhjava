package ccm.cva.issuance.presentation.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditIssuanceHistoryResponse(
        UUID issuanceId,
        UUID requestId,
        UUID ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        BigDecimal co2ReducedKg,
        BigDecimal creditsRaw,
        BigDecimal creditsRounded,
        String idempotencyKey,
        String correlationId,
        Instant issuedAt,
        Instant verifiedAt,
        UUID verifierId,
        String notes
) {
}
