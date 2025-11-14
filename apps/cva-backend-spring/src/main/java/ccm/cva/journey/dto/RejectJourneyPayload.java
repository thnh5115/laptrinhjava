package ccm.cva.journey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RejectJourneyPayload(
        @NotNull Long verifierId,
        @NotBlank String reason
) {}
