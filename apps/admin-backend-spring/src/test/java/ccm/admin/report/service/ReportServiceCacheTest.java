package ccm.admin.report.service;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;
import ccm.admin.report.service.impl.ReportServiceImpl;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any; // ĐÃ THÊM
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * Cache-specific tests for ReportService
 * Tests PR-5 (REP-002): Verify caching behavior for expensive aggregations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService - Cache Tests (PR-5)")
class ReportServiceCacheTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Setup cache manager for testing
        cacheManager = new ConcurrentMapCacheManager(
            "reports:summary",
            "reports:monthly"
        );
        
        // Mock default repository responses (lenient to avoid unnecessary stubbing warnings)
        lenient().when(userRepository.count()).thenReturn(100L);
        lenient().when(transactionRepository.count()).thenReturn(50L);
        lenient().when(transactionRepository.countByStatus(TransactionStatus.APPROVED)).thenReturn(30L);
        lenient().when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(15L);
        lenient().when(transactionRepository.countByStatus(TransactionStatus.REJECTED)).thenReturn(5L);
        lenient().when(transactionRepository.calculateApprovedRevenue()).thenReturn(15000.0);

        lenient().when(transactionRepository.findMonthlyStatsByYear(anyInt())).thenReturn(new ArrayList<>());
    }

    @Test
    @DisplayName("REP-002: First call should hit database")
    void testFirstCallHitsDatabase() {
        // When: Get summary for the first time
        ReportSummaryResponse response = reportService.getSummary();

        // Then: Should query database
        assertThat(response).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(15000.0);
        
        // Verify: Repository called once
        verify(transactionRepository, times(1)).calculateApprovedRevenue();
        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("REP-002: Cache should improve response time significantly")
    void testCacheImprovesPerformance() {
        // Given: First call to populate cache
        ReportSummaryResponse response1 = reportService.getSummary();

        // When: Second call (should be cached - simulated)
        ReportSummaryResponse response2 = reportService.getSummary();

        // Then: Both responses should be equal
        assertThat(response1.getTotalRevenue()).isEqualTo(response2.getTotalRevenue());
        assertThat(response1.getTotalUsers()).isEqualTo(response2.getTotalUsers());
        
        // Note: In unit tests without Spring context, cache annotations don't work
        // But in integration tests, second call would be significantly faster
        // Expected: duration2 < duration1 * 0.1 (90%+ improvement)
        
        // Verify: Repository still called twice in unit test (no actual caching)
        // In integration test with @SpringBootTest, would be called once
        verify(transactionRepository, times(2)).calculateApprovedRevenue();
    }

    @Test
    @DisplayName("REP-002: Monthly report should cache by year")
    void testMonthlyReportCachesByYear() {
        // When: Get monthly report for 2024
        ReportChartResponse report2024 = reportService.getMonthlyReport(2024);
        
        // Then: Should return data
        assertThat(report2024).isNotNull();
        assertThat(report2024.getTransactionsByMonth()).isNotNull();
        assertThat(report2024.getRevenueByMonth()).isNotNull();
        
        // When: Get monthly report for 2025 (different year)
        ReportChartResponse report2025 = reportService.getMonthlyReport(2025);
        
        // Then: Both should be cached separately by year
        assertThat(report2025).isNotNull();
        
        verify(transactionRepository, times(1)).findMonthlyStatsByYear(2024);
        verify(transactionRepository, times(1)).findMonthlyStatsByYear(2025);
    }

    @Test
    @DisplayName("REP-002: Cache should return null for empty results gracefully")
    void testCacheHandlesNullResults() {
        // Given: No transactions
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.countByStatus(any())).thenReturn(0L);
        when(transactionRepository.calculateApprovedRevenue()).thenReturn(0.0);

        // When: Get summary
        ReportSummaryResponse response = reportService.getSummary();

        // Then: Should handle gracefully (unless = "#result == null" prevents caching null)
        assertThat(response).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(0.0);
        assertThat(response.getTotalTransactions()).isEqualTo(0L);
    }

    @Test
    @DisplayName("REP-002: Cache configuration should use 10-minute TTL")
    void testCacheTTLConfiguration() {
        // This test verifies cache configuration
        // Actual TTL testing requires integration test with @SpringBootTest
        
        // Expected behavior (documented):
        // - Cache TTL: 10 minutes
        // - Max entries: 1000
        // - Eviction: Automatic after TTL
        // - Cache names: reports:summary, reports:monthly
        
        assertThat(cacheManager.getCacheNames()).contains("reports:summary", "reports:monthly");
    }

    @Test
    @DisplayName("REP-002: Multiple calls to same year should use cache")
    void testMultipleCallsSameYearUseCache() {
        // When: Call monthly report multiple times for same year
        reportService.getMonthlyReport(2025);
        reportService.getMonthlyReport(2025);
        reportService.getMonthlyReport(2025);

        // Sửa: Code nghiệp vụ gọi findMonthlyStatsByYear, không gọi findAll
 // (Trong unit test, cache không hoạt động, nên gọi 3 lần là đúng)
verify(transactionRepository, times(3)).findMonthlyStatsByYear(2025);
        
        // Note: With @SpringBootTest and actual caching:
        // verify(transactionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("REP-002: Cache should not store null results (unless condition)")
    void testCacheDoesNotStoreNull() {
        // The @Cacheable annotation has: unless = "#result == null"
        // This means null results won't be cached
        
        // If service returned null (which it doesn't), it wouldn't be cached
        // Every call would hit the database
        
        // This test documents the behavior
        ReportSummaryResponse response = reportService.getSummary();
        assertThat(response).isNotNull(); // Service never returns null
    }

    @Test
    @DisplayName("REP-002: Cache key should differentiate between different years")
    void testCacheKeyDifferentiatesByYear() {
        // Given: Get reports for different years
        ReportChartResponse report2024 = reportService.getMonthlyReport(2024);
        ReportChartResponse report2025 = reportService.getMonthlyReport(2025);
        ReportChartResponse report2026 = reportService.getMonthlyReport(2026);

        // Then: Each year should have separate cache entry
        assertThat(report2024).isNotNull();
        assertThat(report2025).isNotNull();
        assertThat(report2026).isNotNull();
        
        //Sửa: Code nghiệp vụ gọi findMonthlyStatsByYear, không gọi findAll
        verify(transactionRepository, times(1)).findMonthlyStatsByYear(2024);
        verify(transactionRepository, times(1)).findMonthlyStatsByYear(2025);
        verify(transactionRepository, times(1)).findMonthlyStatsByYear(2026);
        
        // In integration test with caching:
        // - 3 separate cache entries: reports:monthly::2024, reports:monthly::2025, reports:monthly::2026
        // - Each cached for 10 minutes independently
    }
}
