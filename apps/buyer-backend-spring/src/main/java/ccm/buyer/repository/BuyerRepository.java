package ccm.buyer.repository;

import ccm.buyer.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    Optional<Buyer> findByIdAndRole(Long id, String role);

}
