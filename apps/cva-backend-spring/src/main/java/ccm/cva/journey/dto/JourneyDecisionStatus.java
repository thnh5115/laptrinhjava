package ccm.cva.journey.dto;

import ccm.admin.journey.entity.enums.JourneyStatus;

public enum JourneyDecisionStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static JourneyDecisionStatus fromJourneyStatus(JourneyStatus status) {
        return switch (status) {
            case PENDING -> PENDING;
            case VERIFIED -> APPROVED;
            case REJECTED -> REJECTED;
        };
    }

    public JourneyStatus toJourneyStatus() {
        return switch (this) {
            case PENDING -> JourneyStatus.PENDING;
            case APPROVED -> JourneyStatus.VERIFIED;
            case REJECTED -> JourneyStatus.REJECTED;
        };
    }
}
