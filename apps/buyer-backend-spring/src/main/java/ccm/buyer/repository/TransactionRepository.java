package ccm.buyer.repository;

import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.TrStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByBuyerId(Long buyerId);
  List<Transaction> findByStatus(TrStatus status);
}
