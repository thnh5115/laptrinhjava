package ccm.owner.repo;

import ccm.owner.entitys.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface WalletRepository extends JpaRepository<Wallet, Long> {

}
