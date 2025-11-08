package ccm.cva.verification.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ChecksumPreviewRequest(
        @NotNull(message = "ownerId is required") UUID ownerId,
        @NotBlank(message = "tripId is required") String tripId,
        @NotNull(message = "distanceKm is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "distanceKm must be greater than zero") BigDecimal distanceKm,
        @NotNull(message = "energyKwh is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "energyKwh must be greater than zero") BigDecimal energyKwh
) {}
