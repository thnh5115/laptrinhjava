package ccm.admin.report.service;

import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.entity.enums.DisputeStatus;
import ccm.admin.dispute.repository.DisputeRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportServiceImpl
 * 
 * Tests PR-1 (REP-001): Revenue calculation must ONLY count APPROVED transactions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService - Revenue Calculation Tests (PR-1)")
class ReportServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private DisputeRepository disputeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        // Common setup for all tests
        when(userRepository.count()).thenReturn(100L);
        when(transactionRepository.count()).thenReturn(50L);
    }

    @Test
    @DisplayName("REP-001: Should calculate revenue from APPROVED transactions only")
    void testRevenueCalculation_OnlyApprovedTransactions() {
        // Given: Mock repository to return revenue from APPROVED transactions
        when(transactionRepository.countByStatus(TransactionStatus.APPROVED)).thenReturn(30L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(15L);
        when(transactionRepository.countByStatus(TransactionStatus.REJECTED)).thenReturn(5L);
        when(transactionRepository.calculateApprovedRevenue()).thenReturn(15000.0);

        // When: Get summary report
        ReportSummaryResponse response = reportService.getSummary();

        // Then: Revenue should only count APPROVED transactions
        assertThat(response).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(15000.0);
        assertThat(response.getApprovedTransactions()).isEqualTo(30L);
        assertThat(response.getPendingTransactions()).isEqualTo(15L);
        assertThat(response.getRejectedTransactions()).isEqualTo(5L);

        // Verify: Repository method called exactly once with APPROVED status
        verify(transactionRepository, times(1)).calculateApprovedRevenue();
        verify(transactionRepository, never()).findAll();  // CRITICAL: Should NOT load all transactions
    }

    @Test
    @DisplayName("REP-001: Should return 0 revenue when no APPROVED transactions exist")
    void testRevenueCalculation_NoApprovedTransactions() {
        // Given: No APPROVED transactions (all PENDING/REJECTED)
        when(transactionRepository.countByStatus(TransactionStatus.APPROVED)).thenReturn(0L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(20L);
        when(transactionRepository.countByStatus(TransactionStatus.REJECTED)).thenReturn(10L);
        when(transactionRepository.calculateApprovedRevenue()).thenReturn(0.0);

        // When: Get summary report
        ReportSummaryResponse response = reportService.getSummary();

        // Then: Revenue should be 0 (COALESCE ensures non-null)
        assertThat(response).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(0.0);
        assertThat(response.getApprovedTransactions()).isEqualTo(0L);
        assertThat(response.getTotalTransactions()).isEqualTo(50L);  // Total count includes all statuses

        // Verify: Database-level aggregation used
        verify(transactionRepository, times(1)).calculateApprovedRevenue();
        verify(transactionRepository, never()).findAll();
    }

    @Test
    @DisplayName("REP-001: Should return 0 revenue when database is empty")
    void testRevenueCalculation_EmptyDatabase() {
        // Given: No transactions at all
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.countByStatus(any())).thenReturn(0L);
        when(transactionRepository.calculateApprovedRevenue()).thenReturn(0.0);

        // When: Get summary report
        ReportSummaryResponse response = reportService.getSummary();

        // Then: All counts should be 0, revenue should be 0.0 (not null)
        assertThat(response).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(0.0);
        assertThat(response.getTotalTransactions()).isEqualTo(0L);
        assertThat(response.getApprovedTransactions()).isEqualTo(0L);

        // Verify: Query executed even for empty DB (COALESCE returns 0.0)
        verify(transactionRepository, times(1)).calculateApprovedRevenue();
    }

    @Test
    @DisplayName("REP-001: Should handle large revenue amounts correctly")
    void testRevenueCalculation_LargeAmounts() {
        // Given: Large revenue from APPROVED transactions
        when(transactionRepository.countByStatus(TransactionStatus.APPROVED)).thenReturn(1000L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(0L);
        when(transactionRepository.countByStatus(TransactionStatus.REJECTED)).thenReturn(0L);
        when(transactionRepository.calculateApprovedRevenue()).thenReturn(999999999.99);

        // When: Get summary report
        ReportSummaryResponse response = reportService.getSummary();

        // Then: Large amounts handled correctly
        assertThat(response).isNotNull();
        assertThat(response.getTotalRevenue()).isEqualTo(999999999.99);
        assertThat(response.getApprovedTransactions()).isEqualTo(1000L);

        // Verify: Database aggregation handles large datasets efficiently
        verify(transactionRepository, times(1)).calculateApprovedRevenue();
        verify(transactionRepository, never()).findAll();  // Critical for performance
    }

    @Test
    @DisplayName("REP-001: Should include all report fields correctly")
    void testGetSummary_AllFieldsPopulated() {
        // Given: Mock all repository methods
        when(transactionRepository.countByStatus(TransactionStatus.APPROVED)).thenReturn(25L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(10L);
        when(transactionRepository.countByStatus(TransactionStatus.REJECTED)).thenReturn(15L);
        when(transactionRepository.calculateApprovedRevenue()).thenReturn(5000.0);

        // When: Get summary report
        ReportSummaryResponse response = reportService.getSummary();

        // Then: All fields should be populated correctly
        assertThat(response).isNotNull();
        assertThat(response.getTotalUsers()).isEqualTo(100L);
        assertThat(response.getTotalTransactions()).isEqualTo(50L);
        assertThat(response.getApprovedTransactions()).isEqualTo(25L);
        assertThat(response.getPendingTransactions()).isEqualTo(10L);
        assertThat(response.getRejectedTransactions()).isEqualTo(15L);
        assertThat(response.getTotalRevenue()).isEqualTo(5000.0);

        // Verify: All repository methods called
        verify(userRepository, times(1)).count();
        verify(transactionRepository, times(1)).count();
        verify(transactionRepository, times(1)).calculateApprovedRevenue();
    }
}
