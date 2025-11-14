package ccm.cva.journey.service;

import ccm.cva.journey.dto.JourneyDecisionStatus;
import java.time.LocalDateTime;

public record JourneyQuery(
        JourneyDecisionStatus status,
        Long ownerId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        String search
) {}
