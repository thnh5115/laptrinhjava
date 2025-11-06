package ccm.buyer.repository;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.enums.TrStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CreditOrderRepository extends JpaRepository<CreditOrder, Long> {
    Page<CreditOrder> findByBuyer_Id(Long buyerId, Pageable pageable);
    Page<CreditOrder> findByStatus(TrStatus status, Pageable pageable);
}
