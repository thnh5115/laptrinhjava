package ccm.cva.verification.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ApproveVerificationRequestPayload(
        @NotNull(message = "verifierId is required") Long verifierId,
        String notes,
        @NotBlank(message = "idempotencyKey is required") String idempotencyKey,
        String correlationId
) {}
