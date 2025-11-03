package ccm.cva.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateVerificationRequestPayload(
        @NotNull UUID ownerId,
        @NotBlank String tripId,
        @NotNull @Positive BigDecimal distanceKm,
        @NotNull @Positive BigDecimal energyKwh,
        @NotBlank String checksum,
        String notes
) {}
