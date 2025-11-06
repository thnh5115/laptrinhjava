package ccm.buyer.repository;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CreditOrderRepository extends JpaRepository<CreditOrder, Long>  {

    Page<CreditOrder> findByBuyer_Id(Long buyerId, Pageable pageable);
    Page<CreditOrder> findByStatus(OrderStatus status, Pageable pageable);
    Page<CreditOrder> findByBuyer_IdAndStatus(Long buyerId, OrderStatus status, Pageable pageable);

    List<CreditOrder> findByBuyer_Id(Long buyerId);

    @Query("select co from CreditOrder co where co.buyer.id = :buyerId")
    List<CreditOrder> findByBuyerId(@Param("buyerId") Long buyerId);

}