package ccm.buyer.repository;

import ccm.buyer.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    boolean existsByEmail(String email);
}
