package ccm.buyer.repository;

import ccm.buyer.entity.CreditOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditOrderRepository extends JpaRepository<CreditOrder, Long>  {
    List<CreditOrder> findByBuyerId(Long buyerId);
}
