package ccm.admin.audit.service;

import ccm.admin.audit.repository.HttpAuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * AUD-002: Audit Log Cleanup Job
 * Automatically deletes audit logs older than configured retention period
 * Runs daily at 2 AM to minimize impact on production
 */
@Component
@Slf4j
public class AuditLogCleanupJob {

    private final HttpAuditLogRepository auditLogRepository;
    
    @Value("${audit.retention.days:90}")
    private int retentionDays;

    public AuditLogCleanupJob(HttpAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Delete audit logs older than retention period
     * Runs daily at 2:00 AM (cron: second minute hour day month weekday)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldAuditLogs() {
        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        log.info("Starting audit log cleanup for entries older than {} (retention: {} days)", 
                 cutoffDate, retentionDays);

        try {
            int deleted = auditLogRepository.deleteByCreatedAtBefore(cutoffDate);

            log.info("Successfully deleted {} old audit log entries", deleted);
            
            if (deleted > 0) {
                log.info("Audit log cleanup completed. Freed up storage by removing {} entries", deleted);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs", e);
        }
    }
    
    /**
     * Manual cleanup method for testing or manual execution
     * 
     * @param daysToRetain Number of days to retain (older entries will be deleted)
     * @return Number of deleted entries
     */
    public int cleanupManual(int daysToRetain) {
        Instant cutoffDate = Instant.now().minus(daysToRetain, ChronoUnit.DAYS);
        
        log.info("Manual audit log cleanup for entries older than {} ({} days)", 
                 cutoffDate, daysToRetain);
        
        int deleted = auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
        
        log.info("Manual cleanup completed: {} entries deleted", deleted);
        
        return deleted;
    }
}
