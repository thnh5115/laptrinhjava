package ccm.admin.listing.entity.enums;

/**
 * Status of a carbon credit listing
 * - PENDING: Newly created, awaiting admin approval
 * - APPROVED: Admin approved, visible to buyers
 * - REJECTED: Admin rejected, not visible to buyers
 */
public enum ListingStatus {
    PENDING,
    APPROVED,
    REJECTED
}
