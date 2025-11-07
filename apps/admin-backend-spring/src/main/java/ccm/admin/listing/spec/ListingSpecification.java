package ccm.admin.listing.spec;

import ccm.admin.listing.dto.request.ListingFilterRequest;
import ccm.admin.listing.entity.Listing;
import ccm.admin.listing.entity.enums.ListingStatus;
import ccm.common.spec.BaseSpecification;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/** spec - JPA Specification - Dynamic query builder for spec filters */

public class ListingSpecification {

    
    public static Specification<Listing> filter(ListingFilterRequest req) {
        BaseSpecification<Listing> builder = BaseSpecification.<Listing>builder()
            .keyword(req.getKeyword(), "title")
            .enumEquals("status", req.getStatus(), ListingStatus.class);

        
        if (req.getOwnerEmail() != null && !req.getOwnerEmail().trim().isEmpty()) {
            builder.add((root, query, cb) -> {
                String pattern = "%" + req.getOwnerEmail().toLowerCase().trim() + "%";
                return cb.like(
                    cb.lower(root.join("owner", JoinType.LEFT).get("email")), 
                    pattern
                );
            });
        }

        return builder.build();
    }

    
    public static Specification<Listing> fetchOwner() {
        return BaseSpecification.<Listing>builder()
            .fetch("owner", JoinType.LEFT)
            .build();
    }
}
