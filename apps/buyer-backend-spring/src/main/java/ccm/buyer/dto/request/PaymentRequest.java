package ccm.buyer.dto.request;

import jakarta.validation.constraints.*;

public record PaymentRequest(
    @NotNull Long trId,
    @NotBlank String method,
    @NotNull @Positive Double amount
) {}
