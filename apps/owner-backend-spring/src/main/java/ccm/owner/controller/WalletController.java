package ccm.owner.controller;

import ccm.owner.DTO.SpendRequestDTO;
import ccm.owner.DTO.WalletBalanceDTO;
import ccm.owner.entitys.CarbonCreditTransaction;
import ccm.owner.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    /**
     * GET /api/wallets/{id}/balance
     * Gets the full balance breakdown for a wallet (total, locked, and available).
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<WalletBalanceDTO> getWalletBalance(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(walletService.getWalletBalance(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * GET /api/wallets/{id}/transactions
     * Gets the immutable audit log (transaction history) for a wallet.
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<CarbonCreditTransaction>> getTransactionHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(walletService.getTransactionHistory(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * POST /api/wallets/{id}/retire
     * Retires (spends/destroys) a specified amount of available credits from a wallet.
     * This is the new "spend" endpoint.
     */
    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retireCredits(@PathVariable Long id, @RequestBody SpendRequestDTO retireRequest) {
        try {
            walletService.retireCredits(id, retireRequest.amount());
            return ResponseEntity.ok().build();

        } catch (EntityNotFoundException e) {
            // Wallet not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalStateException e) {
            // Insufficient funds
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IllegalArgumentException e) {
            // e.g., spending zero or a negative amount
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}