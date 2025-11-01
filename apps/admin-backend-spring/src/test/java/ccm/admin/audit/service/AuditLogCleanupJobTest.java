package ccm.admin.audit.service;

import ccm.admin.audit.repository.HttpAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogCleanupJob
 * Tests PR-4 fix: AUD-002 (log retention policy and scheduled cleanup)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogCleanupJob Tests")
class AuditLogCleanupJobTest {

    @Mock
    private HttpAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogCleanupJob cleanupJob;

    private static final int DEFAULT_RETENTION_DAYS = 90;

    @BeforeEach
    void setUp() {
        // Set retention days using ReflectionTestUtils since @Value won't work in unit tests
        ReflectionTestUtils.setField(cleanupJob, "retentionDays", DEFAULT_RETENTION_DAYS);
    }

    // ========================================
    // AUD-002: LOG RETENTION POLICY TESTS
    // ========================================

    @Test
    @DisplayName("AUD-002: Should delete audit logs older than 90 days")
    void testDeleteOldAuditLogs() {
        // Given: 100 old audit logs exist
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(100);

        // When: Scheduled cleanup runs
        cleanupJob.cleanupOldAuditLogs();

        // Then: Repository delete method should be called with correct cutoff date
        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(cutoffCaptor.capture());

        Instant cutoffDate = cutoffCaptor.getValue();
        Instant expectedCutoff = Instant.now().minus(DEFAULT_RETENTION_DAYS, ChronoUnit.DAYS);

        // Allow 1 second tolerance for test execution time
        assertThat(cutoffDate).isBetween(
                expectedCutoff.minusSeconds(1),
                expectedCutoff.plusSeconds(1)
        );
    }

    @Test
    @DisplayName("AUD-002: Should handle zero deleted entries gracefully")
    void testHandleZeroDeletedEntries() {
        // Given: No old logs to delete
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(0);

        // When: Scheduled cleanup runs
        cleanupJob.cleanupOldAuditLogs();

        // Then: Should not throw exception
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("AUD-002: Should handle repository exceptions gracefully")
    void testHandleRepositoryExceptions() {
        // Given: Repository throws exception
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When: Scheduled cleanup runs
        cleanupJob.cleanupOldAuditLogs();

        // Then: Should catch exception and not crash
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("AUD-002: Manual cleanup should use custom retention days")
    void testManualCleanupWithCustomRetention() {
        // Given: Manual cleanup with 30 days retention
        int customRetentionDays = 30;
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(50);

        // When: Manual cleanup is triggered
        int deleted = cleanupJob.cleanupManual(customRetentionDays);

        // Then: Should use custom retention days
        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(cutoffCaptor.capture());

        Instant cutoffDate = cutoffCaptor.getValue();
        Instant expectedCutoff = Instant.now().minus(customRetentionDays, ChronoUnit.DAYS);

        assertThat(cutoffDate).isBetween(
                expectedCutoff.minusSeconds(1),
                expectedCutoff.plusSeconds(1)
        );
        assertThat(deleted).isEqualTo(50);
    }

    @Test
    @DisplayName("AUD-002: Manual cleanup should return number of deleted entries")
    void testManualCleanupReturnCount() {
        // Given: 200 logs will be deleted
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(200);

        // When: Manual cleanup is triggered
        int deleted = cleanupJob.cleanupManual(60);

        // Then: Should return correct count
        assertThat(deleted).isEqualTo(200);
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(any(Instant.class));
    }

    @Test
    @DisplayName("AUD-002: Should respect configured retention days from application.yml")
    void testRespectConfiguredRetentionDays() {
        // Given: Configured retention is 120 days
        ReflectionTestUtils.setField(cleanupJob, "retentionDays", 120);
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(10);

        // When: Scheduled cleanup runs
        cleanupJob.cleanupOldAuditLogs();

        // Then: Should use configured retention (120 days)
        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(cutoffCaptor.capture());

        Instant cutoffDate = cutoffCaptor.getValue();
        Instant expectedCutoff = Instant.now().minus(120, ChronoUnit.DAYS);

        assertThat(cutoffDate).isBetween(
                expectedCutoff.minusSeconds(1),
                expectedCutoff.plusSeconds(1)
        );
    }

    @Test
    @DisplayName("AUD-002: Should delete large numbers of logs efficiently")
    void testDeleteLargeNumbersEfficiently() {
        // Given: 10,000 old logs to delete
        when(auditLogRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(10000);

        // When: Cleanup runs
        cleanupJob.cleanupOldAuditLogs();

        // Then: Should handle large deletes
        verify(auditLogRepository, times(1)).deleteByCreatedAtBefore(any(Instant.class));
    }
}
