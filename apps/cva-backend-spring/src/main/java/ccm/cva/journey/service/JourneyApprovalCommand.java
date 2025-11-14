package ccm.cva.journey.service;

import java.math.BigDecimal;

public record JourneyApprovalCommand(
        Long verifierId,
        BigDecimal overrideCredits
) {}
