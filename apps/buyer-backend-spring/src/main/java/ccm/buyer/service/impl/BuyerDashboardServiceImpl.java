package ccm.buyer.service.impl;

import ccm.buyer.dto.response.BuyerDashboardResponse; // Bạn cần tạo lại file DTO này (xem bước 4)
import ccm.buyer.enums.TrStatus;
import ccm.buyer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BuyerDashboardServiceImpl { // Có thể tạo Interface nếu muốn chuẩn chỉ

    private final TransactionRepository transactionRepository;

    public BuyerDashboardResponse getDashboardStats(Long buyerId) {
        long total = transactionRepository.countByBuyerId(buyerId);
        long pending = transactionRepository.countByBuyerIdAndStatus(buyerId, TrStatus.PENDING);
        long completed = transactionRepository.countByBuyerIdAndStatus(buyerId, TrStatus.COMPLETED);
        BigDecimal spent = transactionRepository.sumAmountByBuyerId(buyerId);

        return BuyerDashboardResponse.builder()
                .totalOrders(total)
                .pendingTransactions(pending)
                .completedTransactions(completed)
                .totalSpent(spent)
                .build();
    }
}