package ccm.buyer.repository;

import ccm.buyer.entity.EWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EWalletRepository extends JpaRepository<EWallet, Long> {
    Optional<EWallet> findByUserId(Long userId);
}
