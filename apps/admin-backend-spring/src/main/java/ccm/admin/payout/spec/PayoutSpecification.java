package ccm.admin.payout.spec;

import ccm.admin.payout.entity.Payout;
import ccm.admin.payout.entity.enums.PayoutStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PayoutSpecification {

    /** Filter by user ID */
    public static Specification<Payout> hasUserId(Long userId) {
        if (userId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    /** Filter by status */
    public static Specification<Payout> hasStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            PayoutStatus enumValue = PayoutStatus.valueOf(status.toUpperCase());
            return (root, query, cb) -> cb.equal(root.get("status"), enumValue);
        } catch (IllegalArgumentException e) {
            return null;  // Invalid status, ignore filter
        }
    }

    /** Filter by requested date from */
    public static Specification<Payout> hasRequestedDateFrom(LocalDateTime fromDate) {
        if (fromDate == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("requestedAt"), fromDate);
    }

    /** Filter by requested date to */
    public static Specification<Payout> hasRequestedDateTo(LocalDateTime toDate) {
        if (toDate == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("requestedAt"), toDate);
    }
}
