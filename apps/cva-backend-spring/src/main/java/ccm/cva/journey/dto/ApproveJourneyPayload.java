package ccm.cva.journey.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ApproveJourneyPayload(
        @NotNull Long verifierId,
        @Positive(message = "Override credits must be positive") BigDecimal overrideCredits
) {}
