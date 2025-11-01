package ccm.admin.transaction.repository;

import ccm.admin.transaction.entity.TransactionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PR-2 (TX-004): Repository for transaction audit logs
 */
@Repository
public interface TransactionAuditLogRepository extends JpaRepository<TransactionAuditLog, Long> {

    /**
     * Find audit logs for a specific transaction
     * 
     * @param transactionId Transaction ID
     * @return List of audit logs ordered by changed_at DESC
     */
    List<TransactionAuditLog> findByTransactionIdOrderByChangedAtDesc(Long transactionId);

    /**
     * Find audit logs by admin email
     * 
     * @param changedBy Admin email
     * @return List of audit logs
     */
    List<TransactionAuditLog> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Count how many times a transaction status was changed
     * 
     * @param transactionId Transaction ID
     * @return Number of status changes
     */
    @Query("SELECT COUNT(t) FROM TransactionAuditLog t WHERE t.transactionId = :transactionId")
    long countByTransactionId(@Param("transactionId") Long transactionId);
}
