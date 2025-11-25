package ccm.admin.listing.entity.enums;

/** enums - Enum - Status states for enums */

public enum ListingStatus {
    PENDING,     // Submitted, waiting for admin approval
    APPROVED,    // Approved and visible on marketplace
    REJECTED,    // Rejected by admin
    DELISTED,
    SOLD     // Removed from marketplace by admin
}
