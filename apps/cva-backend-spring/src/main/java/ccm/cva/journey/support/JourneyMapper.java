package ccm.cva.journey.support;

import ccm.admin.journey.entity.Journey;
import ccm.cva.journey.dto.JourneyDecisionStatus;
import ccm.cva.journey.dto.JourneyResponse;

public final class JourneyMapper {

    private JourneyMapper() {
    }

    public static JourneyResponse toResponse(Journey journey) {
        return new JourneyResponse(
                journey.getId(),
                journey.getUserId(),
                journey.getJourneyDate(),
                journey.getStartLocation(),
                journey.getEndLocation(),
                journey.getDistanceKm(),
                journey.getEnergyUsedKwh(),
                journey.getCreditsGenerated(),
                JourneyDecisionStatus.fromJourneyStatus(journey.getStatus()),
                journey.getVerifiedBy(),
                journey.getVerifiedAt(),
                journey.getRejectionReason(),
                journey.getCreatedAt()
        );
    }
}
