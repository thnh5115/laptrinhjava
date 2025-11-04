package ccm.cva.verification.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RejectVerificationRequestPayload(
        @NotNull(message = "verifierId is required") UUID verifierId,
        @NotBlank(message = "reason is required") String reason
) {}
