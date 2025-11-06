package ccm.owner.controller;

import ccm.owner.DTO.SpendRequestDTO;
import ccm.owner.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // GET /api/wallets/{id}/balance
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletBalance(id));
    }

    // GET /api/wallets/{id}/transactions
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<CarbonCreditTransaction>> getTransactionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getTransactionHistory(id));
    }

    // POST /api/wallets/{id}/spend
    @PostMapping("/{id}/spend")
    public ResponseEntity<Void> spendCredits(@PathVariable Long id, @RequestBody SpendRequestDTO spendRequest) {
        try {
            walletService.spendCredits(id, spendRequest.amount());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build(); // Insufficient funds
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}