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
import ccm.owner.listing.repository.OwnerListingRepository;
import ccm.owner.listing.entity.ListingStatus;
import ccm.owner.listing.entity.Listing;
import ccm.admin.transaction.entity.Transaction;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


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
    private final OwnerListingRepository listingRepository;

    

    /**
     * Get wallet balance for the current user
     */
   @Transactional
    public WalletBalanceResponse getMyBalance() {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        // 1. Tìm hoặc tạo ví tiền (Money Wallet)
        EWallet wallet = eWalletRepository.findByUserId(userId)
                .orElseGet(() -> createWalletForUser(userId));

        // ====================================================
        // PHẦN 1: TÍNH TOÁN TÍN CHỈ (CREDITS LOGIC)
        // ====================================================
        
        // A. Tổng tín chỉ kiếm được (Total Generated)
        BigDecimal totalCreditsGenerated = Optional.ofNullable(
            journeyRepository.sumCreditsByUserId(userId)
        ).orElse(BigDecimal.ZERO);

        List<Listing> soldListings = listingRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.equal(root.get("owner"), currentUser),
                cb.equal(root.get("status"), ListingStatus.SOLD)
            )
        );
        BigDecimal soldCredits = soldListings.stream().map(Listing::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Listing> unavailableListings = listingRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.equal(root.get("owner"), currentUser),
                root.get("status").in(
                    ListingStatus.PENDING, 
                    ListingStatus.APPROVED, 
                    ListingStatus.OPEN,
                    ListingStatus.SOLD 
                )
            )
        );
        
        BigDecimal usedCredits = unavailableListings.stream()
                .map(Listing::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // C. Khả dụng = Tổng kiếm được - Đã sử dụng
        BigDecimal availableCredits = totalCreditsGenerated.subtract(usedCredits);
        
        // Đảm bảo không bị âm
        if (availableCredits.compareTo(BigDecimal.ZERO) < 0) {
            availableCredits = BigDecimal.ZERO; 
        }

        // ====================================================
        // PHẦN 2: TÍNH TOÁN TIỀN (MONEY LOGIC)
        // ====================================================
List<Transaction> mySales = transactionRepository.findAll((root, query, cb) ->
            cb.equal(root.get("sellerId"), userId)
        );
        
          BigDecimal totalEarnings = mySales.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus().name())) 
                .map(Transaction::getTotalPrice) // Lấy tổng tiền mỗi giao dịch
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Tổng tiền đã rút thành công (COMPLETED)
        BigDecimal completedPayouts = Optional.ofNullable(
            payoutRepository.sumAmountByUserIdAndStatus(userId, PayoutStatus.COMPLETED)
        ).orElse(BigDecimal.ZERO);

        // Tổng tiền đã duyệt rút (APPROVED) - Trừ khỏi ví
        BigDecimal approvedPayouts = Optional.ofNullable(
            payoutRepository.sumApprovedAmountByUserId(userId)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalWithdrawals = completedPayouts.add(approvedPayouts);
        
        BigDecimal pendingWithdrawals = Optional.ofNullable(
            payoutRepository.sumPendingAmountByUserId(userId)
        ).orElse(BigDecimal.ZERO);

        log.info("User {}: MoneyBalance=${}, AvailableCredits={}", currentUser.getEmail(), wallet.getBalance(), availableCredits);

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .userEmail(currentUser.getEmail())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .lastUpdated(wallet.getUpdatedAt())
                .soldCredits(soldCredits) // [MỚI] Trả về số đã bán
                
                .totalEarnings(totalEarnings) 
                // Thông tin Credits
                .totalCreditsGenerated(totalCreditsGenerated)
                .availableCredits(availableCredits) // Số dư thực tế để bán
                .lockedCredits(usedCredits)        // Tổng đã dùng
                
                // Thông tin Money
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

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        eWalletRepository.save(wallet);

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
    @Transactional
    public void addCredits(Long userId, BigDecimal amount) {
        log.info("Processing credit addition: userId={}, amount={}", userId, amount);
        
        EWallet wallet = eWalletRepository.findByUserId(userId)
                .orElseGet(() -> createWalletForUser(userId));

        // Cộng dồn số dư
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        
        eWalletRepository.save(wallet);
        
        log.info("Wallet credited successfully. New balance: {}", wallet.getBalance());
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