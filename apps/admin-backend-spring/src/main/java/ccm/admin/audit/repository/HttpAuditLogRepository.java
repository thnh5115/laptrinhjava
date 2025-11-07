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

@Repository
/** repository - Service Interface - Record and query audit logs */

public interface HttpAuditLogRepository extends JpaRepository<HttpAuditLog, Long>, JpaSpecificationExecutor<HttpAuditLog> {
    
    
    Page<HttpAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    
    Page<HttpAuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    
    Page<HttpAuditLog> findByEndpointStartingWithOrderByCreatedAtDesc(String endpointPrefix, Pageable pageable);

    
    Page<HttpAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to, Pageable pageable);
    
    
    List<HttpAuditLog> findByAction(String action);
    
    
    @Modifying
    @Query("DELETE FROM HttpAuditLog h WHERE h.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") Instant cutoffDate);
}
