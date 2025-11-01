package ccm.admin.audit.repository;

import ccm.admin.audit.entity.HttpAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho HTTP request audit logs (request_audit_logs table).
 * Dùng để log tất cả HTTP requests với method, endpoint, body, status.
 */
@Repository
public interface HttpAuditLogRepository extends JpaRepository<HttpAuditLog, Long>, JpaSpecificationExecutor<HttpAuditLog> {
    
    // Lấy log mới nhất trước (phục vụ trang danh sách)
    Page<HttpAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Lọc theo username (admin nào thao tác)
    Page<HttpAuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    // Lọc theo prefix endpoint (ví dụ /api/users, /api/listings)
    Page<HttpAuditLog> findByEndpointStartingWithOrderByCreatedAtDesc(String endpointPrefix, Pageable pageable);

    // Lọc theo khoảng thời gian
    Page<HttpAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to, Pageable pageable);
    
    // Lọc theo action
    List<HttpAuditLog> findByAction(String action);
    
    /**
     * AUD-002: Delete audit logs older than cutoff date
     * Used by scheduled cleanup job to maintain retention policy
     * 
     * @param cutoffDate Logs created before this date will be deleted
     * @return Number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM HttpAuditLog h WHERE h.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") Instant cutoffDate);
}

