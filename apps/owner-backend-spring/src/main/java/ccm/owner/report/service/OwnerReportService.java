package ccm.owner.report.service;

import ccm.owner.report.dto.response.OwnerReportSummaryResponse;
import ccm.owner.report.dto.response.OwnerMonthlyReportResponse;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.payout.repository.PayoutRepository;
import ccm.admin.payout.entity.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerReportService {

    private final JourneyRepository journeyRepository;
    private final TransactionRepository transactionRepository;
    private final PayoutRepository payoutRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public OwnerReportSummaryResponse getSummary() {
        User currentUser = getCurrentUser();
        log.info("Generating summary report for user: {}", currentUser.getEmail());

        // Journey statistics
        long totalJourneys = journeyRepository.count();
        long verifiedJourneys = journeyRepository.countByStatus(JourneyStatus.VERIFIED);
        long pendingJourneys = journeyRepository.countByStatus(JourneyStatus.PENDING);
        long rejectedJourneys = journeyRepository.countByStatus(JourneyStatus.REJECTED);

        // Credits generated
        BigDecimal totalCreditsGenerated = journeyRepository.sumCreditsByUserId(currentUser.getId());
        if (totalCreditsGenerated == null) totalCreditsGenerated = BigDecimal.ZERO;

        // Financial statistics
        BigDecimal totalEarnings = transactionRepository.sumEarningsBySellerEmail(currentUser.getEmail());
        if (totalEarnings == null) totalEarnings = BigDecimal.ZERO;

        // Payout statistics
        BigDecimal totalWithdrawals = payoutRepository.calculateTotalAmountByStatus(PayoutStatus.COMPLETED);
        if (totalWithdrawals == null) totalWithdrawals = BigDecimal.ZERO;

        BigDecimal pendingWithdrawals = payoutRepository.sumPendingAmountByUserId(currentUser.getId());
        if (pendingWithdrawals == null) pendingWithdrawals = BigDecimal.ZERO;

        // Calculate averages
        double averageCreditsPerJourney = verifiedJourneys > 0
                ? totalCreditsGenerated.divide(BigDecimal.valueOf(verifiedJourneys), 2, BigDecimal.ROUND_HALF_UP).doubleValue()
                : 0.0;

        double verificationRate = totalJourneys > 0
                ? ((double) verifiedJourneys / totalJourneys) * 100.0
                : 0.0;

        log.info("Summary generated: {} total journeys, {} credits, ${} earnings",
                totalJourneys, totalCreditsGenerated, totalEarnings);

        return OwnerReportSummaryResponse.builder()
                .totalJourneys(totalJourneys)
                .verifiedJourneys(verifiedJourneys)
                .pendingJourneys(pendingJourneys)
                .rejectedJourneys(rejectedJourneys)
                .totalCreditsGenerated(totalCreditsGenerated)
                .averageCreditsPerJourney(averageCreditsPerJourney)
                .verificationRate(verificationRate)
                .totalEarnings(totalEarnings)
                .totalWithdrawals(totalWithdrawals)
                .pendingWithdrawals(pendingWithdrawals)
                .availableBalance(totalEarnings.subtract(totalWithdrawals).subtract(pendingWithdrawals))
                .build();
    }

    @Transactional(readOnly = true)
    public OwnerMonthlyReportResponse getMonthlyReport(int year) {
        User currentUser = getCurrentUser();
        log.info("Generating monthly report for user: {}, year: {}", currentUser.getEmail(), year);

        Map<String, Long> journeysByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> creditsByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> earningsByMonth = new LinkedHashMap<>();

        // Initialize all months
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%d-%02d", year, month);
            journeysByMonth.put(monthKey, 0L);
            creditsByMonth.put(monthKey, BigDecimal.ZERO);
            earningsByMonth.put(monthKey, BigDecimal.ZERO);
        }

        // Get all verified journeys for the year
        var journeys = journeyRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), currentUser.getId()),
                cb.equal(root.get("status"), JourneyStatus.VERIFIED),
                cb.between(root.get("journeyDate"),
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31))
        ));

        // Aggregate by month
        for (var journey : journeys) {
            LocalDate date = journey.getJourneyDate();
            String monthKey = String.format("%d-%02d", date.getYear(), date.getMonthValue());

            journeysByMonth.merge(monthKey, 1L, Long::sum);

            BigDecimal credits = journey.getCreditsGenerated() != null
                    ? journey.getCreditsGenerated()
                    : BigDecimal.ZERO;
            creditsByMonth.merge(monthKey, credits, BigDecimal::add);
        }

        // Get earnings by month (from transactions)
        var transactions = transactionRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("sellerEmail"), currentUser.getEmail()),
                cb.between(root.get("createdAt"),
                        LocalDate.of(year, 1, 1).atStartOfDay(),
                        LocalDate.of(year, 12, 31).atTime(23, 59, 59))
        ));

        for (var transaction : transactions) {
            LocalDate date = transaction.getCreatedAt().toLocalDate();
            String monthKey = String.format("%d-%02d", date.getYear(), date.getMonthValue());

            BigDecimal amount = transaction.getTotalPrice() != null
                    ? transaction.getTotalPrice()
                    : BigDecimal.ZERO;
            earningsByMonth.merge(monthKey, amount, BigDecimal::add);
        }

        log.info("Monthly report generated for year {} with data for all 12 months", year);

        return OwnerMonthlyReportResponse.builder()
                .year(year)
                .journeysByMonth(journeysByMonth)
                .creditsByMonth(creditsByMonth)
                .earningsByMonth(earningsByMonth)
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "User not found: " + email));
    }
}