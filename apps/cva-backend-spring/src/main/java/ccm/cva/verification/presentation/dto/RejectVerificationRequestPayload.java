package ccm.cva.verification.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record RejectVerificationRequestPayload(
        @NotNull(message = "verifierId is required") Long verifierId,
        @NotBlank(message = "reason is required") String reason
) {}
