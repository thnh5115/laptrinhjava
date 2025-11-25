package ccm.owner.wallet.service;

import ccm.owner.payout.dto.request.WithdrawalRequest;
import ccm.owner.wallet.dto.response.WalletBalanceResponse;
import ccm.owner.wallet.entity.EWallet;
import ccm.owner.wallet.repository.EWalletRepository;
import ccm.admin.payout.entity.Payout;
import ccm.admin.payout.entity.enums.PayoutStatus;
import ccm.admin.payout.repository.PayoutRepository;
import ccm.admin.journey.entity.Journey;
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

        // 1. Lấy ví (Giữ nguyên)
        EWallet wallet = eWalletRepository.findByUserId(userId)
                .orElseGet(() -> createWalletForUser(userId));

        // 2. Fix Logic tính Tín chỉ (Hỗ trợ cả VERIFIED của CVA)
        // Lấy danh sách hành trình đã được duyệt (bao gồm cả VERIFIED và APPROVED)
        List<Journey> verifiedJourneys = journeyRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), userId),
                cb.or(
                    cb.equal(root.get("status").as(String.class), "VERIFIED"), // CVA dùng cái này
                    cb.equal(root.get("status").as(String.class), "APPROVED")  // Admin dùng cái này
                )
            )
        );
        
        BigDecimal totalCreditsGenerated = verifiedJourneys.stream()
                .map(j -> j.getCreditsGenerated() != null ? j.getCreditsGenerated() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Fix Logic tính Listing đang khóa (Thêm SOLD vào Enum ListingStatus ở Bước 1 trước nhé)
        // Tính tổng tín chỉ đang bán hoặc đã bán
        List<Listing> allListings = listingRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("owner"), currentUser)
        );
        
        BigDecimal lockedCredits = allListings.stream()
            .filter(l -> "PENDING".equals(l.getStatus().name()) || 
                         "APPROVED".equals(l.getStatus().name()) || 
                         "OPEN".equals(l.getStatus().name()))
            .map(Listing::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soldCredits = allListings.stream()
            .filter(l -> "SOLD".equals(l.getStatus().name()))
            .map(Listing::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Tính khả dụng: Tổng kiếm - (Đang bán + Đã bán)
        BigDecimal availableCredits = totalCreditsGenerated.subtract(lockedCredits).subtract(soldCredits);
        if (availableCredits.compareTo(BigDecimal.ZERO) < 0) availableCredits = BigDecimal.ZERO;

        // 5. [FIX QUAN TRỌNG] Tính tiền kiếm được (Total Earnings)
        // Cách an toàn: Lấy Listing của user -> Lấy Transaction tương ứng
        BigDecimal totalEarnings = BigDecimal.ZERO;
        try {
            // Lấy list ID các bài đăng của user
            List<Long> myListingIds = allListings.stream().map(Listing::getId).toList();
            
            if (!myListingIds.isEmpty()) {
                // Tìm transaction liên quan đến các listing này
                // Lưu ý: Cách này an toàn hơn tìm theo email
                List<Transaction> transactions = transactionRepository.findAll(); // Lấy tạm all rồi lọc (hoặc viết query findAllByListingIdIn)
                
                totalEarnings = transactions.stream()
                    .filter(t -> myListingIds.contains(t.getListingId()))
                    .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus().name()))
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO) // transaction dùng 'amount' hoặc 'totalPrice' tùy entity
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception e) {
            log.error("Error calculating earnings: {}", e.getMessage());
        }

        // ... (Phần Withdrawals giữ nguyên) ...
        BigDecimal approvedPayouts = Optional.ofNullable(payoutRepository.sumApprovedAmountByUserId(userId)).orElse(BigDecimal.ZERO);
        BigDecimal completedPayouts = Optional.ofNullable(payoutRepository.sumAmountByUserIdAndStatus(userId, PayoutStatus.COMPLETED)).orElse(BigDecimal.ZERO);
        BigDecimal totalWithdrawals = approvedPayouts.add(completedPayouts);
        BigDecimal pendingWithdrawals = Optional.ofNullable(payoutRepository.sumPendingAmountByUserId(userId)).orElse(BigDecimal.ZERO);

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .userEmail(currentUser.getEmail())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .lastUpdated(wallet.getUpdatedAt())
                .totalCreditsGenerated(totalCreditsGenerated)
                .availableCredits(availableCredits)
                .lockedCredits(lockedCredits)
                .soldCredits(soldCredits)
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