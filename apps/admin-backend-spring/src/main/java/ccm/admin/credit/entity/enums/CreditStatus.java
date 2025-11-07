package ccm.admin.credit.entity.enums;

/** enums - Enum - Status categories for CarbonCredit availability */

public enum CreditStatus {
    AVAILABLE,   // Generated from verified journey, not yet listed
    LISTED,      // Listed on marketplace for sale
    SOLD,        // Purchased by buyer
    RESERVED     // Temporarily reserved during purchase
}
