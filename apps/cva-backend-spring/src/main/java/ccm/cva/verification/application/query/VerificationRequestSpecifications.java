package ccm.cva.verification.application.query;

import ccm.cva.verification.domain.VerificationRequest;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class VerificationRequestSpecifications {

    private VerificationRequestSpecifications() {
    }

    public static Specification<VerificationRequest> fromQuery(VerificationRequestQuery query) {
        Specification<VerificationRequest> spec = Specification.where(null);
        if (query == null) {
            return spec;
        }

        if (query.status() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), query.status()));
        }
        if (query.ownerId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("ownerId"), query.ownerId()));
        }
        Instant createdFrom = query.createdFrom();
        if (createdFrom != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
        }
        Instant createdTo = query.createdTo();
        if (createdTo != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
        }
        if (StringUtils.hasText(query.search())) {
            String pattern = "%" + query.search().trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("tripId")), pattern),
                cb.like(cb.lower(root.get("checksum")), pattern)
            ));
        }
        return spec;
    }
}
