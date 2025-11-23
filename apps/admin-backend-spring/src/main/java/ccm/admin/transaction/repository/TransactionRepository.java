package ccm.admin.transaction.repository;

import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.projection.TransactionMonthlyStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/** repository - Service Interface - repository business logic and data operations */

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    
    
    long countByStatus(TransactionStatus status);
    
    @Query("SELECT COALESCE(SUM(t.totalPrice), 0) FROM Transaction t WHERE t.status IN :statuses")
    BigDecimal sumTotalAmountByStatuses(@Param("statuses") List<TransactionStatus> statuses);
    
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM transactions WHERE status = :status", nativeQuery = true)
    BigDecimal sumTotalAmountByStatus(@Param("status") String status);
    
    
    default double calculateApprovedRevenue() {
        BigDecimal total = sumTotalAmountByStatuses(List.of(TransactionStatus.APPROVED, TransactionStatus.COMPLETED));
        return total != null ? total.doubleValue() : 0.0;
    }
    
    
    @Query(value = """
            SELECT 
                MONTH(created_at)   AS month,
                COUNT(*)            AS transactionCount,
                COALESCE(SUM(CASE WHEN status IN ('APPROVED','COMPLETED') THEN total_amount ELSE 0 END), 0) AS approvedRevenue
            FROM transactions
            WHERE YEAR(created_at) = :year
            GROUP BY MONTH(created_at)
            ORDER BY month
            """, nativeQuery = true)
    List<TransactionMonthlyStatsProjection> findMonthlyStatsByYear(@Param("year") int year);
    
    /**
     * Count transactions involving a specific user (as buyer or seller)
     * @param userEmail The user email
     * @return Number of transactions
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.buyerEmail = :userEmail OR t.sellerEmail = :userEmail")
    long countByUserEmail(@Param("userEmail") String userEmail);
    
    /**
     * Sum total earnings for a seller (COMPLETED transactions)
     * @param sellerEmail The seller user email
     * @return Total earnings
     */
    @Query("SELECT COALESCE(SUM(t.totalPrice), 0) FROM Transaction t WHERE t.sellerEmail = :sellerEmail AND t.status = 'COMPLETED'")
    BigDecimal sumEarningsBySellerEmail(@Param("sellerEmail") String sellerEmail);
    
    /**
     * Sum total spending for a buyer (COMPLETED transactions)
     * @param buyerEmail The buyer user email
     * @return Total spending
     */
    @Query("SELECT COALESCE(SUM(t.totalPrice), 0) FROM Transaction t WHERE t.buyerEmail = :buyerEmail AND t.status = 'COMPLETED'")
    BigDecimal sumSpendingByBuyerEmail(@Param("buyerEmail") String buyerEmail);
}
