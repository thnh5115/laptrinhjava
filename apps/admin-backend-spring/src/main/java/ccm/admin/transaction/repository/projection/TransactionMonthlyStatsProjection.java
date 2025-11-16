package ccm.admin.transaction.repository.projection;

import java.math.BigDecimal;

/**
 * Projection for aggregated monthly transaction statistics used by analytics.
 */
public interface TransactionMonthlyStatsProjection {
    
    Integer getMonth();
    
    Long getTransactionCount();
    
    BigDecimal getApprovedRevenue();
}
