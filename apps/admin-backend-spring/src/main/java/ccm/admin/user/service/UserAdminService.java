package ccm.admin.user.service;

import ccm.admin.dispute.repository.DisputeRepository;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.admin.listing.repository.ListingRepository;
import ccm.admin.payout.repository.PayoutRepository;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.user.dto.response.UserOverviewResponse;
import ccm.admin.user.dto.response.UserSummaryResponse;
import ccm.common.dto.paging.PageResponse;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.spec.UserSpecification;
import ccm.common.util.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * service - Service Implementation - Admin operations with validation and audit
 */

/**
 * @summary <business action>
 */
public class UserAdminService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final JourneyRepository journeyRepository;
    private final PayoutRepository payoutRepository;

    /**
     * Process business logic - transactional
     */
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> searchUsers(
            Integer page, Integer size, String sort,
            String role, String status, String keyword
    ) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 200) ? 20 : size;

        Sort sortSpec = SortUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(p, s, sortSpec);

        AccountStatus statusEnum = parseStatus(status);
        Specification<User> spec = Specification.where(UserSpecification.fetchRole())
                .and(UserSpecification.statusEquals(statusEnum))
                .and(UserSpecification.roleEquals(role))
                .and(UserSpecification.keywordLike(keyword));

        Page<User> pageData = userRepository.findAll(spec, pageable);

        var content = pageData.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageResponse<>(
                content,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isFirst(),
                pageData.isLast(),
                (sort == null ? "" : sort)
        );
    }

    private UserSummaryResponse toDto(User u) {
        return new UserSummaryResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole() == null ? null : u.getRole().getName(),
                u.getStatus() == null ? null : u.getStatus().name(),
                u.getCreatedAt()
        );
    }

    private AccountStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Get comprehensive overview of user's activity and statistics READ-ONLY
     * operation for admin monitoring
     *
     * @param userId The user ID to get overview for
     * @return UserOverviewResponse with all statistics
     */
    @Transactional(readOnly = true)
    public UserOverviewResponse getUserOverview(Long userId) {
        log.debug("Getting overview for user ID: {}", userId);

        // Get user entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Count listings (OWNER only)
        long listingCount = listingRepository.countByOwner(user);

        // Count transactions (buyer or seller) - use email
        long transactionCount = transactionRepository.countByUserEmail(user.getEmail());

        // Count disputes raised by user - use email
        long disputeCount = disputeRepository.countByUserEmail(user.getEmail());

        // Count journeys created by user
        long journeyCount = journeyRepository.countByUserId(userId);

        // Financial statistics - use email for transactions
        BigDecimal totalCreditsGenerated = journeyRepository.sumCreditsByUserId(userId);
        BigDecimal totalEarnings = transactionRepository.sumEarningsBySellerEmail(user.getEmail());
        BigDecimal totalSpending = transactionRepository.sumSpendingByBuyerEmail(user.getEmail());

        // Payout statistics (OWNER only)
        long payoutRequestCount = payoutRepository.countByUserId(userId);
        BigDecimal pendingPayoutAmount = payoutRepository.sumPendingAmountByUserId(userId);
        BigDecimal approvedPayoutAmount = payoutRepository.sumApprovedAmountByUserId(userId);

        // Calculate wallet balance (earnings - payouts)
        BigDecimal walletBalance = totalEarnings.subtract(approvedPayoutAmount).subtract(pendingPayoutAmount);

        log.info("Retrieved overview for user {}: {} listings, {} transactions, {} disputes",
                userId, listingCount, transactionCount, disputeCount);

        return UserOverviewResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(null) // Not available in User entity
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(null) // Not available in User entity
                .listingCount(listingCount)
                .transactionCount(transactionCount)
                .disputeCount(disputeCount)
                .journeyCount(journeyCount)
                .walletBalance(walletBalance)
                .totalCreditsGenerated(totalCreditsGenerated)
                .totalEarnings(totalEarnings)
                .totalSpending(totalSpending)
                .payoutRequestCount(payoutRequestCount)
                .pendingPayoutAmount(pendingPayoutAmount)
                .approvedPayoutAmount(approvedPayoutAmount)
                .build();
    }
}
