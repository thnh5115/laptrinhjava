package ccm.cva.verification.presentation.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditIssuanceResponse(
        UUID id,
        BigDecimal co2ReducedKg,
        BigDecimal creditsRaw,
        BigDecimal creditsRounded,
        String idempotencyKey,
        String correlationId,
        Instant createdAt
) {}
