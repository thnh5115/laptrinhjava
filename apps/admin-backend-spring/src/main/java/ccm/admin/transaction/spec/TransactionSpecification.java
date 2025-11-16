package ccm.admin.transaction.spec;

import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.entity.enums.TransactionType;
import ccm.common.spec.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

/** spec - JPA Specification - Dynamic query builder for spec filters */

public class TransactionSpecification {
    
    
    public static Specification<Transaction> filter(String keyword, String status, String type) {
        return BaseSpecification.<Transaction>builder()
            .keyword(keyword, "buyerEmail", "sellerEmail", "transactionCode")
            .enumEquals("status", status, TransactionStatus.class)
            .enumEquals("type", type, TransactionType.class)
            .build();
    }
}
