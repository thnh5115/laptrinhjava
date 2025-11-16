package ccm.admin.dispute.spec;

import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.entity.enums.DisputeStatus;
import ccm.common.spec.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

/** spec - JPA Specification - Dynamic query builder for spec filters */

public class DisputeSpecification {

    
    public static Specification<Dispute> filter(String keyword, String status) {
        return BaseSpecification.<Dispute>builder()
            .keyword(keyword, "description")
            .enumEquals("status", status, DisputeStatus.class)
            .build();
    }
}
