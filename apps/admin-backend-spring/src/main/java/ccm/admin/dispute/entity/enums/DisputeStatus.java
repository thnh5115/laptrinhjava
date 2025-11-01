package ccm.admin.dispute.entity.enums;

/**
 * Enum for Dispute Status
 * OPEN - New dispute raised
 * IN_REVIEW - Admin is reviewing the dispute
 * RESOLVED - Dispute resolved successfully
 * REJECTED - Dispute rejected by admin
 */
public enum DisputeStatus {
    OPEN,
    IN_REVIEW,
    RESOLVED,
    REJECTED
}
