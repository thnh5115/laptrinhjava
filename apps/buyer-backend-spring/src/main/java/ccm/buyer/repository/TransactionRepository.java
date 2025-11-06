package ccm.buyer.repository;

import ccm.buyer.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByOrderBuyerId(Long buyerId);
}
