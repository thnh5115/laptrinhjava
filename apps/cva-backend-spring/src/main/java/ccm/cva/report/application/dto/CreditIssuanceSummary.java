package ccm.cva.report.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditIssuanceSummary(
        UUID id,
        BigDecimal co2ReducedKg,
        BigDecimal creditsRaw,
        BigDecimal creditsRounded,
        String idempotencyKey,
        String correlationId,
        Instant createdAt
) {}
