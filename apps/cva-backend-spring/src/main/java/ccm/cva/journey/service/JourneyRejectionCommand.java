package ccm.cva.journey.service;

public record JourneyRejectionCommand(
        Long verifierId,
        String reason
) {}
