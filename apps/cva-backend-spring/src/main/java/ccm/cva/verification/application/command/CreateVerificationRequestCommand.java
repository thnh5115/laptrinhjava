package ccm.cva.verification.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateVerificationRequestCommand(
        UUID ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        String notes
) {}
