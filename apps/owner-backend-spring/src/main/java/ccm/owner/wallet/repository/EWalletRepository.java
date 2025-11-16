package ccm.owner.wallet.repository;

import ccm.owner.wallet.entity.EWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repository for EWallet operations
 */
public interface EWalletRepository extends JpaRepository<EWallet, Long> {

    /**
     * Find wallet by user ID
     */
    Optional<EWallet> findByUserId(Long userId);

    /**
     * Check if wallet exists for user
     */
    boolean existsByUserId(Long userId);
}