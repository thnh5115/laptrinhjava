package ccm.admin.transaction.spec;

import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.entity.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic Transaction filtering
 */
public class TransactionSpecification {
    
    /**
     * Build dynamic filter specification
     * 
     * @param keyword Search in buyer/seller email
     * @param status Filter by transaction status
     * @param type Filter by transaction type
     * @return Specification for filtering
     */
    public static Specification<Transaction> filter(String keyword, String status, String type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by keyword (search in buyer_email or seller_email)
            if (keyword != null && !keyword.isBlank()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("buyerEmail")), searchPattern),
                    cb.like(cb.lower(root.get("sellerEmail")), searchPattern),
                    cb.like(cb.lower(root.get("transactionCode")), searchPattern)
                ));
            }

            // Filter by status
            if (status != null && !status.isBlank()) {
                try {
                    TransactionStatus statusEnum = TransactionStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    // Invalid status value, ignore filter
                }
            }

            // Filter by type
            if (type != null && !type.isBlank()) {
                try {
                    TransactionType typeEnum = TransactionType.valueOf(type.toUpperCase());
                    predicates.add(cb.equal(root.get("type"), typeEnum));
                } catch (IllegalArgumentException e) {
                    // Invalid type value, ignore filter
                }
            }

            // Return combined predicates
            return predicates.isEmpty() 
                ? cb.conjunction() 
                : cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
