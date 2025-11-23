package ccm.cva.verification.application.command;

import java.math.BigDecimal;


public record CreateVerificationRequestCommand(
        Long ownerId,
        String tripId,
        BigDecimal distanceKm,
        BigDecimal energyKwh,
        String checksum,
        String notes
) {}
