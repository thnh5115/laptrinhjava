package ccm.admin.transaction.repository;

import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Transaction entity with dynamic query support
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    
    /**
     * Count transactions by status
     * 
     * @param status Transaction status to filter by
     * @return Number of transactions with the given status
     */
    long countByStatus(TransactionStatus status);
    
    /**
     * Calculate total revenue from APPROVED transactions only
     * Uses COALESCE to return 0.0 instead of NULL when no transactions exist
     * 
     * CRITICAL: Only counts APPROVED transactions for accurate financial reporting
     * 
     * @param status Transaction status (should be APPROVED)
     * @return Total revenue from transactions with given status, never null
     */
    @Query("SELECT COALESCE(SUM(t.totalPrice), 0.0) FROM Transaction t WHERE t.status = :status")
    Double sumTotalPriceByStatus(@Param("status") TransactionStatus status);
    
    /**
     * Calculate total revenue from APPROVED transactions only (convenience method)
     * 
     * @return Total revenue from approved transactions
     */
    default Double calculateApprovedRevenue() {
        return sumTotalPriceByStatus(TransactionStatus.APPROVED);
    }
}
