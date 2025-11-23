package ccm.cva.verification.presentation.dto;

import java.math.BigDecimal;

import java.time.LocalDateTime;


public record CreditIssuanceResponse(
        Long id,
        BigDecimal co2ReducedKg,
        BigDecimal creditsRaw,
        BigDecimal creditsRounded,
        String idempotencyKey,
        String correlationId,
        LocalDateTime createdAt
) {}
