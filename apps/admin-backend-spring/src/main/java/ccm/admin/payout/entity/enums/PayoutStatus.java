package ccm.admin.payout.entity.enums;

public enum PayoutStatus {
    PENDING,      // Owner submitted withdrawal request, waiting for admin review
    APPROVED,     // Admin approved, ready for processing
    REJECTED,     // Admin rejected the request
    COMPLETED     // Payment has been processed and sent to owner
}
