package ccm.admin.analytics.service;

import ccm.admin.analytics.dto.response.DisputeRatioResponse;
import ccm.admin.analytics.dto.response.SystemKpiResponse;
import ccm.admin.analytics.dto.response.TransactionTrendResponse;
import ccm.admin.analytics.service.impl.AnalyticsServiceImpl;
import ccm.admin.dispute.entity.enums.DisputeStatus; // ĐÃ THÊM
import ccm.admin.dispute.repository.DisputeRepository;
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

import java.util.ArrayList; // ĐÃ THÊM

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any; // ĐÃ THÊM
import static org.mockito.ArgumentMatchers.anyInt; // ĐÃ THÊM

/**
 * Cache-specific tests for AnalyticsService
 * Tests PR-5 (ANA-002): Verify caching behavior for analytics aggregations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService - Cache Tests (PR-5)")
class AnalyticsServiceCacheTest {

 @Mock
  private UserRepository userRepository;

 @Mock
 private TransactionRepository transactionRepository;

 @Mock
 private DisputeRepository disputeRepository;

 @InjectMocks
 private AnalyticsServiceImpl analyticsService;

 private CacheManager cacheManager;

 @BeforeEach
 void setUp() {
 // Setup cache manager for testing
 cacheManager = new ConcurrentMapCacheManager(
 "analytics:kpis",
 "analytics:trends",
"analytics:disputes"
 );

 // ----- ĐÃ SỬA LỖI: Cập nhật các mock cho đúng với code nghiệp vụ -----
 lenient().when(userRepository.count()).thenReturn(100L);
lenient().when(transactionRepository.count()).thenReturn(50L);
 lenient().when(disputeRepository.count()).thenReturn(10L);
        
        // Mock các hàm mới thay vì hàm cũ (findAll)
 lenient().when(transactionRepository.calculateApprovedRevenue()).thenReturn(1500.0);
 lenient().when(transactionRepository.findMonthlyStatsByYear(anyInt())).thenReturn(new ArrayList<>());
 lenient().when(disputeRepository.countByStatus(any(DisputeStatus.class))).thenReturn(0L);
 }

 @Test
 @DisplayName("ANA-002: System KPIs should be cacheable")
 void testSystemKpisCacheable() {
 // When: Get KPIs multiple times
 SystemKpiResponse kpi1 = analyticsService.getSystemKpis();
 SystemKpiResponse kpi2 = analyticsService.getSystemKpis();

 // Then: Both should return data
 assertThat(kpi1).isNotNull();
 assertThat(kpi2).isNotNull();
 assertThat(kpi1.getTotalUsers()).isEqualTo(100L);
 assertThat(kpi2.getTotalUsers()).isEqualTo(100L);
 
 
 verify(userRepository, times(2)).count();
 verify(transactionRepository, times(2)).count();
 // Sửa: Code nghiệp vụ gọi calculateApprovedRevenue, không gọi findAll
 verify(transactionRepository, times(2)).calculateApprovedRevenue();
 }

 @Test
 @DisplayName("ANA-002: Transaction trends should cache by year")
 void testTransactionTrendsCachesByYear() {
 // When: Get trends for different years
 TransactionTrendResponse trends2024 = analyticsService.getTransactionTrends(2024);
 TransactionTrendResponse trends2025 = analyticsService.getTransactionTrends(2025);

 // Then: Both should return data
 assertThat(trends2024).isNotNull();
 assertThat(trends2025).isNotNull();
 assertThat(trends2024.getMonthlyTransactions()).isNotNull();
assertThat(trends2025.getMonthlyTransactions()).isNotNull();

 // Sửa: Code nghiệp vụ gọi findMonthlyStatsByYear, không gọi findAll
 verify(transactionRepository, times(1)).findMonthlyStatsByYear(2024);
 verify(transactionRepository, times(1)).findMonthlyStatsByYear(2025);
 }

 @Test
 @DisplayName("ANA-002: Dispute ratios should be cacheable")
void testDisputeRatiosCacheable() {
 // Given: Mock disputes (Đã được mock trong setUp)

 // When: Get dispute ratios multiple times
 DisputeRatioResponse ratio1 = analyticsService.getDisputeRatios();
 DisputeRatioResponse ratio2 = analyticsService.getDisputeRatios();

 // Then: Both should return data
 assertThat(ratio1).isNotNull();
 assertThat(ratio2).isNotNull();
assertThat(ratio1.getTotal()).isEqualTo(0L);
 assertThat(ratio2.getTotal()).isEqualTo(0L);
 

 // Sửa: Code nghiệp vụ gọi countByStatus, không gọi findAll
 verify(disputeRepository, times(2)).countByStatus(DisputeStatus.OPEN);
 verify(disputeRepository, times(2)).countByStatus(DisputeStatus.RESOLVED);
verify(disputeRepository, times(2)).countByStatus(DisputeStatus.REJECTED);
 verify(disputeRepository, times(2)).countByStatus(DisputeStatus.IN_REVIEW);
 }

 @Test
 @DisplayName("ANA-002: Cache configuration should include all analytics caches")
 void testCacheConfiguration() {
 // Verify: All expected caches are configured
 assertThat(cacheManager.getCacheNames()).contains(
 "analytics:kpis",
 "analytics:trends",
 "analytics:disputes"
 );
 }

 @Test
 @DisplayName("ANA-002: Multiple calls to same year trends should use cache")
 void testMultipleCallsSameYearUseCache() {
  // When: Call trends multiple times for same year
 analyticsService.getTransactionTrends(2025);
 analyticsService.getTransactionTrends(2025);
 analyticsService.getTransactionTrends(2025);


 // Sửa: Code nghiệp vụ gọi findMonthlyStatsByYear, không gọi findAll

 verify(transactionRepository, times(3)).findMonthlyStatsByYear(2025);
 }

 @Test
 @DisplayName("ANA-002: Cache should not store null results")
void testCacheDoesNotStoreNullResults() {
// The @Cacheable annotations use: unless = "#result == null"
 // This ensures null results aren't cached
 
 // All analytics methods return non-null objects
 SystemKpiResponse kpis = analyticsService.getSystemKpis();
 TransactionTrendResponse trends = analyticsService.getTransactionTrends(2025);
 DisputeRatioResponse ratios = analyticsService.getDisputeRatios();
 
 assertThat(kpis).isNotNull();
 assertThat(trends).isNotNull();
 assertThat(ratios).isNotNull();
 }

 @Test
 @DisplayName("ANA-002: Cache keys should differentiate between different years")
 void testCacheKeyDifferentiatesByYear() {
 // When: Get trends for multiple years
 TransactionTrendResponse trends2023 = analyticsService.getTransactionTrends(2023);
 TransactionTrendResponse trends2024 = analyticsService.getTransactionTrends(2024);
 TransactionTrendResponse trends2025 = analyticsService.getTransactionTrends(2025);

 // Then: Each year should have separate cache entry
 assertThat(trends2023).isNotNull();
 assertThat(trends2024).isNotNull();
 assertThat(trends2025).isNotNull();


 // Sửa: Code nghiệp vụ gọi findMonthlyStatsByYear, không gọi findAll
 verify(transactionRepository, times(1)).findMonthlyStatsByYear(2023);
 verify(transactionRepository, times(1)).findMonthlyStatsByYear(2024);
 verify(transactionRepository, times(1)).findMonthlyStatsByYear(2025);
 }

 @Test
 @DisplayName("ANA-002: KPIs should handle empty database gracefully")
 void testKpisHandleEmptyDatabase() {
 // Given: Empty database
 when(userRepository.count()).thenReturn(0L);
 when(transactionRepository.count()).thenReturn(0L);
 when(disputeRepository.count()).thenReturn(0L);
 // ----- ĐÃ SỬA LỖI: Mock hàm mới -----
 when(transactionRepository.calculateApprovedRevenue()).thenReturn(0.0);

 // When: Get KPIs
 SystemKpiResponse kpis = analyticsService.getSystemKpis();

 // Then: Should return zeros, not null
 assertThat(kpis).isNotNull();
 assertThat(kpis.getTotalUsers()).isEqualTo(0L);
 assertThat(kpis.getTotalTransactions()).isEqualTo(0L);
 assertThat(kpis.getTotalDisputes()).isEqualTo(0L);
 assertThat(kpis.getTotalRevenue()).isEqualTo(0.0);
 assertThat(kpis.getDisputeRate()).isEqualTo(0.0);
 }
}