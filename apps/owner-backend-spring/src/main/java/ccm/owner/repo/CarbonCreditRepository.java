package ccm.owner.repo;

import ccm.owner.entitys.CarbonCredit;
import ccm.owner.entitys.CreditStatus;
import ccm.owner.entitys.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

interface CarbonCreditRepository extends JpaRepository<CarbonCredit, Long> {
    // Find all available credits for a specific wallet
    List<CarbonCredit> findByWalletAndStatus(Wallet wallet, CreditStatus status);

    // This is a helper query you'll need for the WalletService
    // to find available credits to retire or sell.
    List<CarbonCredit> findByWalletAndStatusOrderByCreatedAtAsc(Wallet wallet, CreditStatus status);

    // A query to recalculate the wallet's cached balance if it ever gets out of sync
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CarbonCredit c WHERE c.wallet = :wallet AND c.status = :status")
    BigDecimal sumAmountByWalletAndStatus(Wallet wallet, CreditStatus status);
}
