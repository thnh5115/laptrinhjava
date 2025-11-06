package ccm.owner.repo;

import ccm.owner.entitys.CarbonCreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CarbonCreditTransactionRepository extends JpaRepository<CarbonCreditTransaction, Long> {

    // Find all history for a specific wallet (where it was either the source or destination)
    @Query("SELECT t FROM CarbonCreditTransaction t " +
            "WHERE t.sourceWallet.id = :walletId OR t.destinationWallet.id = :walletId " +
            "ORDER BY t.timestamp DESC")
    List<CarbonCreditTransaction> findHistoryByWalletId(Long walletId);
}