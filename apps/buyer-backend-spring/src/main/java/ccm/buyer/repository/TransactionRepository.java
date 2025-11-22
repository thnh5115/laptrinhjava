package ccm.buyer.repository;

import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.TrStatus;
import org.springframework.data.domain.Page; // Thêm import
import org.springframework.data.domain.Pageable; // Thêm import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Thêm import
import org.springframework.data.repository.query.Param; // Thêm import

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Lấy danh sách giao dịch của Buyer (Thay thế getOrdersByBuyer)
    List<Transaction> findByBuyerId(Long buyerId);
    
    // Phân trang (Thay thế list orders)
    Page<Transaction> findByBuyerId(Long buyerId, Pageable pageable);
    
    // Đếm theo trạng thái để làm Dashboard
    long countByBuyerIdAndStatus(Long buyerId, TrStatus status);
    long countByBuyerId(Long buyerId);
    
    // Tính tổng tiền đã chi tiêu (Thay thế totalSpent trong dashboard)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.buyerId = :buyerId AND t.status = 'COMPLETED'")
    java.math.BigDecimal sumAmountByBuyerId(@Param("buyerId") Long buyerId);
}