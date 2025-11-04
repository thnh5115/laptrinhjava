package ccm.buyer.repository;

import ccm.buyer.entity.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BuyerRepository extends JpaRepository<Buyer, Long> {   
    Page<Buyer> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
}
