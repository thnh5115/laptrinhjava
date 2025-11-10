package ccm.owner.service;

import ccm.owner.DTO.WalletBalanceDTO;
import ccm.owner.entitys.*;
import ccm.owner.repo.CarbonCreditRepository;
import ccm.owner.repo.CarbonCreditTransactionRepository;
import ccm.owner.repo.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CarbonCreditRepository creditRepository;
    private final CarbonCreditTransactionRepository transactionRepository;

    private Wallet getWalletOrThrow(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found with id: " + walletId));
    }

    /**
     * Creates a new CarbonCredit "token" and assigns it to a wallet.
     * This replaces the old "addCredits" method.
     */
    @Transactional
    public void createNewCreditToken(Wallet wallet, BigDecimal amount, Journey sourceJourney) {
        // 1. Create the new Credit "Token"
        CarbonCredit newCredit = new CarbonCredit();
        newCredit.setWallet(wallet);
        newCredit.setAmount(amount);
        newCredit.setSourceJourney(sourceJourney);
        newCredit.setStatus(CreditStatus.AVAILABLE);
        CarbonCredit savedCredit = creditRepository.save(newCredit);

        // 2. Update the wallet's cached balance
        wallet.setTotalBalance(wallet.getTotalBalance().add(amount));
        walletRepository.save(wallet);

        // 3. Create the historical audit log
        CarbonCreditTransaction tx = new CarbonCreditTransaction();
        tx.setCredit(savedCredit);
        tx.setTransactionType("EARNED");
        tx.setAmount(amount);
        tx.setDestinationWallet(wallet); // "To" this wallet
        tx.setSourceWallet(null);          // From nowhere (it was created)
        transactionRepository.save(tx);
    }

    /**
     * "Spends" or "destroys" credits by retiring them.
     * This replaces the old "spendCredits" method.
     */
    @Transactional
    public void retireCredits(Long walletId, BigDecimal amountToRetire) {
        Wallet wallet = getWalletOrThrow(walletId);

        if (amountToRetire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to retire must be positive");
        }

        // Check against AVAILABLE balance
        if (wallet.getAvailableBalance().compareTo(amountToRetire) < 0) {
            throw new IllegalStateException("Insufficient available funds. Available: " + wallet.getAvailableBalance());
        }

        // --- Coin Selection Logic ---
        // Find available credits to cover the amount, oldest first.
        List<CarbonCredit> creditsToRetire = new ArrayList<>();
        BigDecimal amountCovered = BigDecimal.ZERO;

        List<CarbonCredit> availableCredits = creditRepository
                .findByWalletAndStatusOrderByCreatedAtAsc(wallet, CreditStatus.AVAILABLE);

        for (CarbonCredit credit : availableCredits) {
            if (amountCovered.compareTo(amountToRetire) >= 0) {
                break; // We have covered the amount
            }

            BigDecimal amountNeeded = amountToRetire.subtract(amountCovered);

            if (credit.getAmount().compareTo(amountNeeded) <= 0) {
                // Retire this entire credit token
                credit.setStatus(CreditStatus.RETIRED);
                creditsToRetire.add(credit);
                amountCovered = amountCovered.add(credit.getAmount());
            } else {
                // This credit is larger than we need. Split it.
                // 1. Create a new "retired" token for the amount we need
                CarbonCredit retiredPortion = new CarbonCredit();
                retiredPortion.setWallet(wallet); // It's still "owned" by them, just retired
                retiredPortion.setAmount(amountNeeded);
                retiredPortion.setStatus(CreditStatus.RETIRED);
                retiredPortion.setSourceJourney(credit.getSourceJourney()); // Keep original source
                creditsToRetire.add(retiredPortion);

                // 2. Reduce the amount of the original "available" token
                credit.setAmount(credit.getAmount().subtract(amountNeeded));

                amountCovered = amountCovered.add(amountNeeded);
            }
        }

        // Save all changes to the credit tokens
        creditRepository.saveAll(availableCredits); // Saves changes to existing
        creditRepository.saveAll(creditsToRetire);  // Saves new "split" tokens

        // Update the wallet's cached balance
        wallet.setTotalBalance(wallet.getTotalBalance().subtract(amountToRetire));
        walletRepository.save(wallet);

        // Create audit logs for each retired token
        for (CarbonCredit retiredCredit : creditsToRetire) {
            CarbonCreditTransaction tx = new CarbonCreditTransaction();
            tx.setCredit(retiredCredit);
            tx.setTransactionType("RETIRED");
            tx.setAmount(retiredCredit.getAmount());
            tx.setSourceWallet(wallet); // "From" this wallet
            tx.setDestinationWallet(null); // To nowhere (it was destroyed)
            transactionRepository.save(tx);
        }
    }

    /**
     * Gets the full balance breakdown for a wallet.
     */
    public WalletBalanceDTO getWalletBalance(Long walletId) {
        Wallet wallet = getWalletOrThrow(walletId);
        return new WalletBalanceDTO(
                wallet.getTotalBalance(),
                wallet.getLockedBalance(),
                wallet.getAvailableBalance()
        );
    }

    /**
     * Gets the full, immutable transaction history for a wallet.
     */
    public List<CarbonCreditTransaction> getTransactionHistory(Long walletId) {
        // Ensure wallet exists before fetching history
        getWalletOrThrow(walletId);
        return transactionRepository.findHistoryByWalletId(walletId);
    }
}