package ccm.admin.analytics.service.impl;

import ccm.admin.analytics.dto.response.DisputeRatioResponse;
import ccm.admin.analytics.dto.response.SystemKpiResponse;
import ccm.admin.analytics.dto.response.TransactionTrendResponse;
import ccm.admin.analytics.service.AnalyticsService;
import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.entity.enums.DisputeStatus;
import ccm.admin.dispute.repository.DisputeRepository;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.TransactionRepository;
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
/** Analytics - Service Implementation - Business logic for Analytics operations */

public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;

    
    /** Process business logic - cached result, transactional */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics:kpis", unless = "#result == null")
    public SystemKpiResponse getSystemKpis() {
        log.info("Calculating system KPIs");

        
        long totalUsers = userRepository.count();
        log.debug("Total users: {}", totalUsers);

        
        long totalTransactions = transactionRepository.count();
        log.debug("Total transactions: {}", totalTransactions);

        
        long totalDisputes = disputeRepository.count();
        log.debug("Total disputes: {}", totalDisputes);

        
        List<Transaction> allTransactions = transactionRepository.findAll();
        double totalRevenue = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.APPROVED)
                .mapToDouble(Transaction::getTotalPrice)
                .sum();
        log.debug("Total revenue: ${}", totalRevenue);

        
        double disputeRate = totalTransactions == 0 ? 0.0 :
                ((double) totalDisputes / totalTransactions) * 100;
        log.debug("Dispute rate: {}%", disputeRate);

        SystemKpiResponse response = SystemKpiResponse.builder()
                .totalUsers(totalUsers)
                .totalTransactions(totalTransactions)
                .totalDisputes(totalDisputes)
                .totalRevenue(totalRevenue)
                .disputeRate(disputeRate)
                .build();

        log.info("System KPIs calculated successfully");
        return response;
    }

    
    /** Process business logic - cached result, transactional */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics:trends", key = "#year", unless = "#result == null")
    public TransactionTrendResponse getTransactionTrends(int year) {
        log.info("Calculating transaction trends for year: {}", year);

        List<Transaction> allTransactions = transactionRepository.findAll();
        Map<String, Long> monthlyTransactions = new LinkedHashMap<>();
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();

        
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            String monthKey = String.format("%d-%02d", year, month);

            
            long transactionCount = allTransactions.stream()
                    .filter(t -> t.getCreatedAt().getYear() == year 
                              && t.getCreatedAt().getMonthValue() == currentMonth)
                    .count();

            
            double revenue = allTransactions.stream()
                    .filter(t -> t.getStatus() == TransactionStatus.APPROVED
                              && t.getCreatedAt().getYear() == year
                              && t.getCreatedAt().getMonthValue() == currentMonth)
                    .mapToDouble(Transaction::getTotalPrice)
                    .sum();

            monthlyTransactions.put(monthKey, transactionCount);
            monthlyRevenue.put(monthKey, revenue);

            log.debug("Month {}: {} transactions, ${} revenue", monthKey, transactionCount, revenue);
        }

        TransactionTrendResponse response = TransactionTrendResponse.builder()
                .monthlyTransactions(monthlyTransactions)
                .monthlyRevenue(monthlyRevenue)
                .build();

        log.info("Transaction trends calculated successfully for year {}", year);
        return response;
    }

    
    /** Process business logic - cached result, transactional */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics:disputes", unless = "#result == null")
    public DisputeRatioResponse getDisputeRatios() {
        log.info("Calculating dispute ratio statistics");

        List<Dispute> allDisputes = disputeRepository.findAll();

        
        long openCount = allDisputes.stream()
                .filter(d -> d.getStatus() == DisputeStatus.OPEN)
                .count();

        long resolvedCount = allDisputes.stream()
                .filter(d -> d.getStatus() == DisputeStatus.RESOLVED)
                .count();

        long rejectedCount = allDisputes.stream()
                .filter(d -> d.getStatus() == DisputeStatus.REJECTED)
                .count();

        long inReviewCount = allDisputes.stream()
                .filter(d -> d.getStatus() == DisputeStatus.IN_REVIEW)
                .count();

        long total = allDisputes.size();

        log.debug("Dispute breakdown - Open: {}, In Review: {}, Resolved: {}, Rejected: {}, Total: {}",
                openCount, inReviewCount, resolvedCount, rejectedCount, total);

        DisputeRatioResponse response = DisputeRatioResponse.builder()
                .openCount(openCount)
                .resolvedCount(resolvedCount)
                .rejectedCount(rejectedCount)
                .total(total)
                .build();

        log.info("Dispute ratio statistics calculated successfully");
        return response;
    }
}
