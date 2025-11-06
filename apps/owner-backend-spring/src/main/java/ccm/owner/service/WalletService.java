package ccm.owner.service;


import ccm.owner.entitys.CarbonCreditTransaction;
import ccm.owner.entitys.Journey;
import ccm.owner.entitys.Wallet;
import ccm.owner.repo.CarbonCreditTransactionRepository;
import ccm.owner.repo.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor // Automatically injects final fields via constructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CarbonCreditTransactionRepository transactionRepository;

    private Wallet getWalletOrThrow(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found with id: " + walletId));
    }

    @Transactional
    public void addCredits(Long walletId, BigDecimal amountToAdd, Journey sourceJourney) {
        if (amountToAdd.compareTo(BigDecimal.ZERO) <= 0) {
            return; // Don't add zero or negative credits
        }

        Wallet wallet = getWalletOrThrow(walletId);

        // 1. Create the transaction record
        CarbonCreditTransaction transaction = new CarbonCreditTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amountToAdd);
        transaction.setTransactionType("EARNED");
        transaction.setSourceJourney(sourceJourney);
        transactionRepository.save(transaction);

        // 2. Update the wallet's cached balance
        wallet.setBalance(wallet.getBalance().add(amountToAdd));
        walletRepository.save(wallet);
    }

    @Transactional
    public void spendCredits(Long walletId, BigDecimal amountToSpend) {
        if (amountToSpend.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to spend must be positive");
        }

        Wallet wallet = getWalletOrThrow(walletId);

        // Check for sufficient funds
        if (wallet.getBalance().compareTo(amountToSpend) < 0) {
            throw new IllegalStateException("Insufficient funds. Available: " + wallet.getBalance());
        }

        // 1. Create the "SPENT" transaction
        CarbonCreditTransaction transaction = new CarbonCreditTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amountToSpend.negate()); // Store as negative
        transaction.setTransactionType("SPENT");
        transactionRepository.save(transaction);

        // 2. Update the wallet's balance
        wallet.setBalance(wallet.getBalance().subtract(amountToSpend));
        walletRepository.save(wallet);
    }

    public BigDecimal getWalletBalance(Long walletId) {
        return getWalletOrThrow(walletId).getBalance();
    }

    public List<CarbonCreditTransaction> getTransactionHistory(Long walletId) {
        // First, ensure wallet exists
        getWalletOrThrow(walletId);
        // Then, fetch its history
        return transactionRepository.findByWalletIdOrderByTimestampDesc(walletId);
    }
}