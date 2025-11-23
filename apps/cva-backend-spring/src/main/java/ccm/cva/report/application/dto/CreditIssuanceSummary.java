package ccm.cva.report.application.dto;

import java.math.BigDecimal;

import java.time.LocalDateTime;


public record CreditIssuanceSummary(
        Long id,
        BigDecimal co2ReducedKg,
        BigDecimal creditsRaw,
        BigDecimal creditsRounded,
        String idempotencyKey,
        String correlationId,
        LocalDateTime createdAt
) {}
