package ccm.owner.service;


import ccm.owner.entitys.*;
import ccm.owner.repo.CarbonCreditTransactionRepository;
import ccm.owner.repo.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CarbonCreditRepository creditRepository; // <-- New repo
    private final CarbonCreditTransactionRepository transactionRepository;

    @Transactional
    public void createNewCreditToken(Wallet wallet, BigDecimal amount, Journey sourceJourney) {
        // 1. Create the new Credit "Token"
        CarbonCredit newCredit = new CarbonCredit();
        newCredit.setWallet(wallet);
        newCredit.setAmount(amount);
        newCredit.setSourceJourney(sourceJourney);
        newCredit.setStatus(CreditStatus.AVAILABLE);
        creditRepository.save(newCredit);

        // 2. Update the wallet's cached balance
        wallet.setTotalBalance(wallet.getTotalBalance().add(amount));
        walletRepository.save(wallet);

        // 3. (Optional) Create a historical log
        createTransaction(wallet, "EARNED", amount);
    }

    // This is "spending" or "retiring" credits
    @Transactional
    public void retireCredits(Wallet wallet, BigDecimal amountToRetire) {
        if (wallet.getAvailableBalance().compareTo(amountToRetire) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        // 1. Find available credits (this is the tricky part)
        // You need to find one or more "AVAILABLE" credits
        // to cover the amount. This is a "coin selection" algorithm.
        // For simplicity, we'll assume a single credit or split one.
        // ... (Logic to find and split/update CarbonCredit entities) ...
        // ... (e.g., find credits, change their status to RETIRED) ...

        // 2. Update the wallet's cached balance
        wallet.setTotalBalance(wallet.getTotalBalance().subtract(amountToRetire));
        walletRepository.save(wallet);

        // 3. Create historical log
        createTransaction(wallet, "RETIRED", amountToRetire.negate());
    }

    // Helper for logging
    private void createTransaction(Wallet wallet, String type, BigDecimal amount) {
        CarbonCreditTransaction tx = new CarbonCreditTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        transactionRepository.save(tx);
    }
}