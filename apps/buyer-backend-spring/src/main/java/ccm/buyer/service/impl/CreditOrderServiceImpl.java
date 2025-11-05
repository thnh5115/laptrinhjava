package ccm.buyer.service.impl;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.entity.OrderStatus;
import ccm.buyer.repository.CreditOrderRepository;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.service.CreditOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditOrderServiceImpl implements CreditOrderService {

    private final CreditOrderRepository creditOrderRepository;

    @Override
    public List<CreditOrder> getAllOrders() {
        return creditOrderRepository.findAll();
    }

    @Override
    public CreditOrder getOrderById(Long id) {
        return creditOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public CreditOrder createOrder(CreditOrder order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        return creditOrderRepository.save(order);
    }

    @Override
    public CreditOrder updateOrder(Long id, CreditOrder order) {
        CreditOrder existing = getOrderById(id);
        existing.setAmount(order.getAmount());
        existing.setPrice(order.getPrice());
        existing.setUpdatedAt(LocalDateTime.now());
        return creditOrderRepository.save(existing);
    }

    @Override
    public void deleteOrder(Long id) {
        creditOrderRepository.deleteById(id);
    }

    @Override
    public CreditOrder updateOrderStatus(Long id, OrderStatus status) {
        CreditOrder order = getOrderById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return creditOrderRepository.save(order);
    }

    @Override
    public List<CreditOrder> getOrdersByBuyer(Long buyerId) {
        return creditOrderRepository.findByBuyerId(buyerId);
    }

    @Override
    public BuyerDashboardResponse getBuyerDashboard(Long buyerId) {
        var orders = creditOrderRepository.findByBuyerId(buyerId);
        long totalOrders = orders.size();
        double totalAmount = orders.stream().mapToDouble(CreditOrder::getAmount).sum();
        double totalSpent = orders.stream().mapToDouble(o -> o.getAmount() * o.getPrice()).sum();

        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long paid = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long approved = orders.stream().filter(o -> o.getStatus() == OrderStatus.APPROVED).count();
        long rejected = orders.stream().filter(o -> o.getStatus() == OrderStatus.REJECTED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();

        return BuyerDashboardResponse.builder()
                .totalOrders(totalOrders)
                .totalAmount(totalAmount)
                .totalSpent(totalSpent)
                .pendingOrders(pending)
                .paidOrders(paid)
                .approvedOrders(approved)
                .rejectedOrders(rejected)
                .cancelledOrders(cancelled)
                .completedOrders(completed)
                .build();
    }

}
