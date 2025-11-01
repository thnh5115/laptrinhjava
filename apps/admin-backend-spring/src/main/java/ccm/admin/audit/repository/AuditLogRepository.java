package ccm.admin.audit.repository;

import ccm.admin.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho business audit logs (audit_logs table).
 * Dùng để tracking business actions: USER_BAN, LISTING_REMOVE, etc.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Tìm theo actor
    List<AuditLog> findByActorId(Long actorId);
    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);
    
    // Tìm theo action type
    List<AuditLog> findByAction(String action);
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    // Tìm theo target
    Page<AuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
        String targetType, String targetId, Pageable pageable);
    
    // Lấy log mới nhất
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Lọc theo khoảng thời gian
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime from, LocalDateTime to, Pageable pageable);
}
