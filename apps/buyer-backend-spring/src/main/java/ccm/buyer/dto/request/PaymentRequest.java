package ccm.buyer.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record PaymentRequest(
    @NotNull Long trId,
    @NotBlank String method,
    @NotNull @Positive BigDecimal amount
) {}
