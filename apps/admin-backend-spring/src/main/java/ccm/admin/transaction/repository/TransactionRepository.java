package ccm.admin.transaction.repository;

import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/** repository - Service Interface - repository business logic and data operations */

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    
    
    long countByStatus(TransactionStatus status);
    
    
    @Query("SELECT COALESCE(SUM(t.totalPrice), 0.0) FROM Transaction t WHERE t.status = :status")
    Double sumTotalPriceByStatus(@Param("status") TransactionStatus status);
    
    
    default Double calculateApprovedRevenue() {
        return sumTotalPriceByStatus(TransactionStatus.APPROVED);
    }
    
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
