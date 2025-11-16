package ccm.admin.transaction.repository;

import ccm.admin.transaction.entity.TransactionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/** repository - Service Interface - Record and query audit logs */

public interface TransactionAuditLogRepository extends JpaRepository<TransactionAuditLog, Long> {

    
    List<TransactionAuditLog> findByTransactionIdOrderByChangedAtDesc(Long transactionId);

    
    List<TransactionAuditLog> findByChangedByOrderByChangedAtDesc(String changedBy);

    
    @Query("SELECT COUNT(t) FROM TransactionAuditLog t WHERE t.transactionId = :transactionId")
    long countByTransactionId(@Param("transactionId") Long transactionId);
}
