package ccm.admin.report.service.impl;

import ccm.admin.report.dto.response.ReportChartResponse;
import ccm.admin.report.dto.response.ReportSummaryResponse;
import ccm.admin.report.service.ReportService;
import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        long approvedTransactions = transactionRepository.countByStatus(TransactionStatus.APPROVED);
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
        
        
        List<Transaction> transactions = transactionRepository.findAll();
        
        
        Map<String, Long> transactionsByMonth = new LinkedHashMap<>();
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        
        
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            String monthKey = String.format("%d-%02d", year, month);
            
            
            long count = transactions.stream()
                    .filter(t -> t.getCreatedAt() != null 
                            && t.getCreatedAt().getYear() == year 
                            && t.getCreatedAt().getMonthValue() == currentMonth)
                    .count();
            
            
            double revenue = transactions.stream()
                    .filter(t -> t.getCreatedAt() != null
                            && t.getStatus() == TransactionStatus.APPROVED
                            && t.getCreatedAt().getYear() == year
                            && t.getCreatedAt().getMonthValue() == currentMonth)
                    .mapToDouble(Transaction::getTotalPrice)
                    .sum();
            
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
