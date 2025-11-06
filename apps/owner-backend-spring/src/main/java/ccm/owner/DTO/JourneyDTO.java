package ccm.owner.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

// Using a record for a simple, immutable DTO
public record JourneyDTO(
        @JsonProperty("distance")
        Double distanceInKm,

        @JsonProperty("date")
        LocalDate journeyDate
) {}