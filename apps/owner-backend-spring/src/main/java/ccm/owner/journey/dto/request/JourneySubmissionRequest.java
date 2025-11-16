package ccm.owner.journey.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Request DTO for submitting a new journey
 * EV Owner submits journey data for carbon credit generation
 */
public class JourneySubmissionRequest {

    @NotNull(message = "Journey date is required")
    @PastOrPresent(message = "Journey date cannot be in the future")
    private LocalDate journeyDate;

    @NotBlank(message = "Start location is required")
    @Size(max = 255, message = "Start location must not exceed 255 characters")
    private String startLocation;

    @NotBlank(message = "End location is required")
    @Size(max = 255, message = "End location must not exceed 255 characters")
    private String endLocation;

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
    @DecimalMax(value = "10000.0", message = "Distance must not exceed 10,000 km")
    private BigDecimal distanceKm;

    @NotNull(message = "Energy used is required")
    @DecimalMin(value = "0.1", message = "Energy used must be at least 0.1 kWh")
    @DecimalMax(value = "1000.0", message = "Energy used must not exceed 1,000 kWh")
    private BigDecimal energyUsedKwh;

    /**
     * Optional: Vehicle identification or registration
     */
    @Size(max = 100, message = "Vehicle ID must not exceed 100 characters")
    private String vehicleId;

    /**
     * Optional: Additional notes or comments
     */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}