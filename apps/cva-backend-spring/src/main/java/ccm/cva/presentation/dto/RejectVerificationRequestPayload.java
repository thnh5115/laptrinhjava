package ccm.cva.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RejectVerificationRequestPayload(
        @NotNull UUID verifierId,
        @NotBlank String reason
) {}
