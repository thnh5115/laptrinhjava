package ccm.owner.repo;

import ccm.owner.entitys.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

interface WalletRepository extends JpaRepository<Wallet, Long> {

}
