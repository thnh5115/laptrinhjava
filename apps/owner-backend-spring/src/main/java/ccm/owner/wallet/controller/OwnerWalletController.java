package ccm.owner.wallet.controller;

import ccm.owner.payout.dto.request.WithdrawalRequest;
import ccm.owner.wallet.dto.response.WalletBalanceResponse;
import ccm.owner.wallet.service.OwnerWalletService;
import ccm.admin.payout.entity.Payout;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ccm.owner.wallet.dto.request.CreditRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/wallet")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('EV_OWNER', 'ADMIN')")
@Tag(name = "EV Owner - Wallet", description = "Wallet and withdrawal management for EV Owners")
/**
 * Controller for EV Owner wallet operations
 */
public class OwnerWalletController {

    private final OwnerWalletService walletService;

    @Operation(summary = "System Credit Addition", description = "Internal API for CVA to issue credits")
    @PostMapping("/credits") // -> URL đầy đủ: /api/owner/wallet/credits
    // Lưu ý: Ở môi trường thật cần bảo mật endpoint này (chỉ cho IP nội bộ hoặc dùng API Key)
    public ResponseEntity<Void> receiveCredits(@RequestBody CreditRequest request) {
        walletService.addCredits(request.getOwnerId(), request.getAmount());
        return ResponseEntity.ok().build();
    }
    @Operation(
            summary = "Get Wallet Balance",
            description = "Retrieve current wallet balance and financial statistics for the authenticated EV Owner"
    )
    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance() {
        log.info("Getting wallet balance for current user");
        WalletBalanceResponse balance = walletService.getMyBalance();
        return ResponseEntity.ok(balance);
    }

    @Operation(
            summary = "Request Withdrawal",
            description = "Submit a withdrawal request (payout) to transfer funds from wallet to bank account"
    )
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequest request) {

        log.info("Processing withdrawal request: amount=${}", request.getAmount());

        Payout payout = walletService.requestWithdrawal(request);

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Withdrawal request submitted successfully. Awaiting admin approval.",
                "payoutId", payout.getId(),
                "amount", payout.getAmount(),
                "status", payout.getStatus().name(),
                "requestedAt", payout.getRequestedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get Withdrawal History",
            description = "Retrieve all withdrawal requests (payouts) for the authenticated EV Owner"
    )
    @GetMapping("/withdrawals")
    public ResponseEntity<List<Payout>> getWithdrawalHistory() {
        log.info("Getting withdrawal history for current user");
        List<Payout> withdrawals = walletService.getMyWithdrawals();
        return ResponseEntity.ok(withdrawals);
    }

    @Operation(
            summary = "Get Withdrawal by ID",
            description = "Retrieve details of a specific withdrawal request"
    )
    @GetMapping("/withdrawals/{id}")
    public ResponseEntity<Payout> getWithdrawalById(@PathVariable Long id) {
        log.info("Getting withdrawal details: id={}", id);

        List<Payout> withdrawals = walletService.getMyWithdrawals();
        Payout payout = withdrawals.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Withdrawal not found or does not belong to you: " + id));

        return ResponseEntity.ok(payout);
    }
}