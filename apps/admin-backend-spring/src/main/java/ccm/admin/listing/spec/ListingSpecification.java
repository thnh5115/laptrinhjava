package ccm.admin.listing.spec;

import ccm.admin.listing.dto.request.ListingFilterRequest;
import ccm.admin.listing.entity.Listing;
import ccm.admin.listing.entity.enums.ListingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic listing queries
 * Supports filtering by keyword, status, and owner email
 */
public class ListingSpecification {

    /**
     * Build specification from filter request
     * @param req Filter criteria
     * @return JPA Specification
     */
    public static Specification<Listing> filter(ListingFilterRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by keyword in title
            if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("title")), 
                        "%" + req.getKeyword().toLowerCase().trim() + "%"
                    )
                );
            }

            // Filter by status
            if (req.getStatus() != null && !req.getStatus().trim().isEmpty()) {
                try {
                    ListingStatus status = ListingStatus.valueOf(req.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Invalid status value, ignore this filter
                }
            }

            // Filter by owner email
            if (req.getOwnerEmail() != null && !req.getOwnerEmail().trim().isEmpty()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.join("owner").get("email")), 
                        "%" + req.getOwnerEmail().toLowerCase().trim() + "%"
                    )
                );
            }

            return predicates.isEmpty() 
                ? cb.conjunction() 
                : cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Fetch owner eagerly to avoid N+1 query problem
     */
    public static Specification<Listing> fetchOwner() {
        return (root, query, cb) -> {
            if (query != null) {
                root.fetch("owner", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }
}
