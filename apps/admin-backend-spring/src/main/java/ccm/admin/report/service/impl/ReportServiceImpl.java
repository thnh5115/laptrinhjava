package ccm.admin.report.service.impl;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;
import ccm.admin.report.service.ReportService;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.transaction.repository.projection.TransactionMonthlyStatsProjection;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/** Report - Service Implementation - Business logic for Report operations */

public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    
    /** Process business logic - cached result, transactional */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reports:summary", unless = "#result == null")
    public ReportSummaryResponse getSummary() {
        log.info("Generating summary report");
        
        
        long totalUsers = userRepository.count();
        
        
        long totalTransactions = transactionRepository.count();
        long approvedTransactions = transactionRepository.countByStatus(TransactionStatus.APPROVED)
                + transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long rejectedTransactions = transactionRepository.countByStatus(TransactionStatus.REJECTED);
        long pendingTransactions = transactionRepository.countByStatus(TransactionStatus.PENDING);
        
        
        
        
        double totalRevenue = transactionRepository.calculateApprovedRevenue();
        
        log.info("Summary: {} users, {} transactions, {} revenue", 
                totalUsers, totalTransactions, totalRevenue);
        
        return ReportSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalTransactions(totalTransactions)
                .approvedTransactions(approvedTransactions)
                .rejectedTransactions(rejectedTransactions)
                .pendingTransactions(pendingTransactions)
                .totalRevenue(totalRevenue)
                .build();
    }

    
    /** Process business logic - cached result, transactional */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reports:monthly", key = "#year", unless = "#result == null")
    public ReportChartResponse getMonthlyReport(int year) {
        log.info("Generating monthly report for year: {}", year);
        
        Map<Integer, TransactionMonthlyStatsProjection> statsByMonth =
                transactionRepository.findMonthlyStatsByYear(year).stream()
                        .collect(Collectors.toMap(
                                TransactionMonthlyStatsProjection::getMonth,
                                Function.identity(),
                                (existing, replacement) -> existing));

        Map<String, Long> transactionsByMonth = new LinkedHashMap<>();
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%d-%02d", year, month);
            TransactionMonthlyStatsProjection stat = statsByMonth.get(month);

            long count = stat != null ? stat.getTransactionCount() : 0L;
            double revenue = stat != null && stat.getApprovedRevenue() != null
                    ? stat.getApprovedRevenue().doubleValue()
                    : 0.0;

            transactionsByMonth.put(monthKey, count);
            revenueByMonth.put(monthKey, revenue);
        }
        
        log.info("Monthly report generated for year {} with {} months of data", 
                year, transactionsByMonth.size());
        
        return ReportChartResponse.builder()
                .transactionsByMonth(transactionsByMonth)
                .revenueByMonth(revenueByMonth)
                .build();
    }
}
