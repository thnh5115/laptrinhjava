package ccm.admin.credit.spec;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.entity.enums.CreditStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class CarbonCreditSpecification {

    /** Filter by owner ID */
    public static Specification<CarbonCredit> hasOwnerId(Long ownerId) {
        if (ownerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    /** Filter by status */
    public static Specification<CarbonCredit> hasStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            CreditStatus enumValue = CreditStatus.valueOf(status.toUpperCase());
            return (root, query, cb) -> cb.equal(root.get("status"), enumValue);
        } catch (IllegalArgumentException e) {
            return null;  // Invalid status, ignore filter
        }
    }

    /** Filter by minimum price */
    public static Specification<CarbonCredit> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        if (minPrice == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("pricePerCredit"), minPrice);
    }

    /** Filter by maximum price */
    public static Specification<CarbonCredit> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        if (maxPrice == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("pricePerCredit"), maxPrice);
    }

    /** Filter by journey ID */
    public static Specification<CarbonCredit> hasJourneyId(Long journeyId) {
        if (journeyId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("journeyId"), journeyId);
    }
}
