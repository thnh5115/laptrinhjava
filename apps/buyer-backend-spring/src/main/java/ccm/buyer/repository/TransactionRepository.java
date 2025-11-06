package ccm.buyer.repository;

import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByOrder_Buyer_Id(Long buyerId, Pageable pageable);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    Page<Transaction> findByOrder_Buyer_IdAndStatus(Long buyerId, TransactionStatus status, Pageable pageable);

    List<Transaction> findByOrder_Buyer_Id(Long buyerId);
    
    @Query("select t from Transaction t where t.order.buyer.id = :buyerId")
    List<Transaction> findByOrderBuyerId(@Param("buyerId") Long buyerId);

}
