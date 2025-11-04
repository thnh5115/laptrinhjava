package ccm.buyer.repository;

import ccm.buyer.entity.CreditOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditOrderRepository extends JpaRepository<CreditOrder, Long> {
}
