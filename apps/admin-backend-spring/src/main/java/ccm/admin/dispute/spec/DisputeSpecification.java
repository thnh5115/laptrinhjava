package ccm.admin.dispute.spec;

import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.entity.enums.DisputeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification for dynamic filtering of disputes
 * Supports filtering by keyword (in description) and status
 */
public class DisputeSpecification {

    /**
     * Create a specification for filtering disputes
     * 
     * @param keyword Search term to filter by description (case-insensitive)
     * @param status Filter by dispute status (OPEN, IN_REVIEW, RESOLVED, REJECTED)
     * @return Specification for the given filters
     */
    public static Specification<Dispute> filter(String keyword, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by keyword in description
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        likePattern
                    )
                );
            }

            // Filter by status
            if (status != null && !status.isBlank()) {
                try {
                    DisputeStatus disputeStatus = DisputeStatus.valueOf(status.toUpperCase());
                    predicates.add(
                        criteriaBuilder.equal(root.get("status"), disputeStatus)
                    );
                } catch (IllegalArgumentException e) {
                    // Invalid status value, ignore this filter
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
