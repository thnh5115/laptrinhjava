package ccm.owner.entitys;

public enum CreditStatus {
    AVAILABLE,      // Can be spent or sold
    LOCKED,         // e.g., Listed for sale
    RETIRED,        // Spent and "destroyed" (cannot be re-sold)
    PENDING         // For verification
}