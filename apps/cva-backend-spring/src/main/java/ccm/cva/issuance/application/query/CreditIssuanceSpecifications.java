package ccm.cva.issuance.application.query;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.verification.domain.VerificationRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CreditIssuanceSpecifications {

    private CreditIssuanceSpecifications() {
    }

    public static Specification<CreditIssuance> fromQuery(CreditIssuanceQuery query) {
        if (query == null) {
            return Specification.where(null);
        }

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<CreditIssuance, VerificationRequest> requestJoin = root.join("verificationRequest", JoinType.LEFT);

            if (query.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), query.ownerId()));
            }

            if (query.createdFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.createdFrom()));
            }

            if (query.createdTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.createdTo()));
            }

            if (StringUtils.hasText(query.search())) {
                String like = "%" + query.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(requestJoin.get("tripId")), like),
                    cb.like(cb.lower(requestJoin.get("checksum")), like),
                    cb.like(cb.lower(root.get("idempotencyKey")), like),
                    cb.like(cb.lower(root.get("correlationId")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
