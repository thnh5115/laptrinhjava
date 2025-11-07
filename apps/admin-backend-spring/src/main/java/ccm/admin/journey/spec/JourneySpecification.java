package ccm.admin.journey.spec;

import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** spec - Specification - Journey query specifications for filtering */

public class JourneySpecification {

    /** Filter by keyword (search in startLocation, endLocation) */
    public static Specification<Journey> hasKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("startLocation")), "%" + keyword.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("endLocation")), "%" + keyword.toLowerCase() + "%")
        );
    }

    /** Filter by status */
    public static Specification<Journey> hasStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            JourneyStatus enumValue = JourneyStatus.valueOf(status.toUpperCase());
            return (root, query, cb) -> cb.equal(root.get("status"), enumValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Filter by date range - from date */
    public static Specification<Journey> hasJourneyDateFrom(LocalDate fromDate) {
        if (fromDate == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("journeyDate"), fromDate);
    }

    /** Filter by date range - to date */
    public static Specification<Journey> hasJourneyDateTo(LocalDate toDate) {
        if (toDate == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("journeyDate"), toDate);
    }

    /** Filter by user ID */
    public static Specification<Journey> hasUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }
}
