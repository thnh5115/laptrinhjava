package ccm.owner.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarbonCreditTransactionRepository extends JpaRepository<CarbonCreditTransaction, Long> {
    List<CarbonCreditTransaction> findByWalletIdOrderByTimestampDesc(Long walletId);
}

