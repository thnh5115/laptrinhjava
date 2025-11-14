package ccm.cva.journey.service;

import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.cva.journey.dto.JourneyDecisionStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class JourneySpecifications {

    private JourneySpecifications() {
    }

    public static Specification<Journey> from(JourneyQuery query) {
        Specification<Journey> spec = Specification.where(null);
        if (query == null) {
            return spec;
        }

        if (query.status() != null) {
            spec = spec.and(statusEquals(query.status()));
        }
        if (query.ownerId() != null) {
            spec = spec.and(ownerEquals(query.ownerId()));
        }
        if (query.createdFrom() != null) {
            spec = spec.and(createdAtAfter(query.createdFrom()));
        }
        if (query.createdTo() != null) {
            spec = spec.and(createdAtBefore(query.createdTo()));
        }
        if (StringUtils.hasText(query.search())) {
            spec = spec.and(matchesSearch(query.search()));
        }
        return spec;
    }

    private static Specification<Journey> statusEquals(JourneyDecisionStatus status) {
        JourneyStatus journeyStatus = status.toJourneyStatus();
        return (root, query, cb) -> cb.equal(root.get("status"), journeyStatus);
    }

    private static Specification<Journey> ownerEquals(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), ownerId);
    }

    private static Specification<Journey> createdAtAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private static Specification<Journey> createdAtBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    private static Specification<Journey> matchesSearch(String term) {
        String likeTerm = "%" + term.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("startLocation")), likeTerm),
                cb.like(cb.lower(root.get("endLocation")), likeTerm)
        );
    }
}
