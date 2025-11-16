package ccm.admin.audit.repository;

import ccm.admin.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
/** repository - Service Interface - Record and query audit logs */

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    
    List<AuditLog> findByActorId(Long actorId);
    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);
    
    
    List<AuditLog> findByAction(String action);
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    
    Page<AuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
        String targetType, String targetId, Pageable pageable);
    
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime from, LocalDateTime to, Pageable pageable);
}
