package ccm.owner.wallet.service;

import ccm.owner.payout.dto.request.WithdrawalRequest;
import ccm.owner.wallet.dto.response.WalletBalanceResponse;
import ccm.owner.wallet.entity.EWallet;
import ccm.owner.wallet.repository.EWalletRepository;
import ccm.admin.payout.entity.Payout;
import ccm.admin.payout.entity.enums.PayoutStatus;
import ccm.admin.payout.repository.PayoutRepository;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Service for EV Owner wallet operations
 */
public class OwnerWalletService {

    private final EWalletRepository eWalletRepository;
    private final UserRepository userRepository;
    private final JourneyRepository journeyRepository;
    private final TransactionRepository transactionRepository;
    private final PayoutRepository payoutRepository;

    /**
     * Get wallet balance for the current user
     */
    @Transactional(readOnly = true)
    public WalletBalanceResponse getMyBalance() {
        User currentUser = getCurrentUser();

        // Find or create wallet
        EWallet wallet = eWalletRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createWalletForUser(currentUser.getId()));

        // Calculate additional metrics
        BigDecimal totalCreditsGenerated = journeyRepository.sumCreditsByUserId(currentUser.getId());
        BigDecimal totalEarnings = transactionRepository.sumEarningsBySellerEmail(currentUser.getEmail());

        BigDecimal approvedPayouts = payoutRepository.sumApprovedAmountByUserId(currentUser.getId());
        BigDecimal completedPayouts = payoutRepository.calculateTotalAmountByStatus(PayoutStatus.COMPLETED);
        BigDecimal totalWithdrawals = approvedPayouts.add(completedPayouts);

        BigDecimal pendingWithdrawals = payoutRepository.sumPendingAmountByUserId(currentUser.getId());

        log.info("Retrieved balance for user {}: ${}", currentUser.getEmail(), wallet.getBalance());

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .userEmail(currentUser.getEmail())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .lastUpdated(wallet.getUpdatedAt())
                .totalCreditsGenerated(totalCreditsGenerated)
                .totalEarnings(totalEarnings)
                .totalWithdrawals(totalWithdrawals)
                .pendingWithdrawals(pendingWithdrawals)
                .build();
    }

    /**
     * Request withdrawal (create payout request)
     */
    @Transactional
    public Payout requestWithdrawal(WithdrawalRequest request) {
        User currentUser = getCurrentUser();

        log.info("Processing withdrawal request for user {}: amount=${}",
                currentUser.getEmail(), request.getAmount());

        // Get wallet
        EWallet wallet = eWalletRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        // Validate balance
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException(
                    String.format("Insufficient balance. Available: $%.2f, Requested: $%.2f",
                            wallet.getBalance(), request.getAmount()));
        }

        // Check for pending withdrawals
        BigDecimal pendingAmount = payoutRepository.sumPendingAmountByUserId(currentUser.getId());
        BigDecimal availableBalance = wallet.getBalance().subtract(pendingAmount);

        if (availableBalance.compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException(
                    String.format("Insufficient available balance after pending withdrawals. Available: $%.2f, Requested: $%.2f",
                            availableBalance, request.getAmount()));
        }

        // Create payout request
        Payout payout = Payout.builder()
                .userId(currentUser.getId())
                .eWalletId(wallet.getId())
                .amount(request.getAmount())
                .status(PayoutStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .bankAccount(request.getBankAccount())
                .notes(request.getNotes())
                .requestedAt(LocalDateTime.now())
                .build();

        Payout savedPayout = payoutRepository.save(payout);

        log.info("Withdrawal request created: id={}, user={}, amount=${}",
                savedPayout.getId(), currentUser.getEmail(), request.getAmount());

        return savedPayout;
    }

    /**
     * Get withdrawal history for current user
     */
    @Transactional(readOnly = true)
    public java.util.List<Payout> getMyWithdrawals() {
        User currentUser = getCurrentUser();

        return payoutRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), currentUser.getId())
        );
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Get currently authenticated user
     */
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

    /**
     * Create wallet for a user (if doesn't exist)
     */
    private EWallet createWalletForUser(Long userId) {
        log.info("Creating wallet for user: {}", userId);

        EWallet wallet = EWallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .status(ccm.owner.wallet.entity.enums.WalletStatus.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();

        return eWalletRepository.save(wallet);
    }
}