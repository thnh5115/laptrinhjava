package ccm.admin.payout.repository;

import ccm.admin.payout.entity.Payout;
import ccm.admin.payout.entity.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long>, JpaSpecificationExecutor<Payout> {

    /** Count payouts by status */
    long countByStatus(PayoutStatus status);

    /** Calculate total payout amount by status */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.status = :status")
    BigDecimal calculateTotalAmountByStatus(PayoutStatus status);

    /** Calculate total amount requested */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p")
    BigDecimal calculateTotalAmountRequested();

    /** Get total count */
    @Query("SELECT COUNT(p) FROM Payout p")
    long getTotalCount();
    
    /**
     * Count payout requests by user
     * @param userId The user ID
     * @return Number of payout requests
     */
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * Sum pending payout amount for a user
     * @param userId The user ID
     * @return Pending payout amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.userId = :userId AND p.status = 'PENDING'")
    BigDecimal sumPendingAmountByUserId(@Param("userId") Long userId);
    
    /**
     * Sum approved payout amount for a user (wallet balance)
     * @param userId The user ID
     * @return Approved payout amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.userId = :userId AND p.status = 'APPROVED'")
    BigDecimal sumApprovedAmountByUserId(@Param("userId") Long userId);
}
