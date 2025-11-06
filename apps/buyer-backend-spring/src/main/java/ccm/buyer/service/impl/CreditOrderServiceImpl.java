package ccm.buyer.service.impl;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.enums.OrderStatus;
import ccm.buyer.repository.CreditOrderRepository;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.dto.response.CreditOrderResponse;
import ccm.buyer.service.CreditOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
        return creditOrderRepository.findByBuyer_Id(buyerId);
    }

    @Override
    public BuyerDashboardResponse getBuyerDashboard(Long buyerId) {
        var list = creditOrderRepository.findByBuyer_Id(buyerId);
        long totalOrders = list.size();
        double totalAmount = list.stream().mapToDouble(CreditOrder::getAmount).sum();
        double totalSpent = list.stream().mapToDouble(o -> o.getAmount() * o.getPrice()).sum();

        long pending = list.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long paid = list.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long approved = list.stream().filter(o -> o.getStatus() == OrderStatus.APPROVED).count();
        long rejected = list.stream().filter(o -> o.getStatus() == OrderStatus.REJECTED).count();
        long cancelled = list.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long completed = list.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();

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

    @Override
    public Page<CreditOrderResponse> list(Long buyerId, String status, Pageable pageable) {
    if (buyerId != null && status != null && !status.isBlank()) {
        OrderStatus st = parseOrderStatus(status);
        return creditOrderRepository
                .findByBuyer_IdAndStatus(buyerId, st, pageable)
                .map(CreditOrderResponse::of);
    }
    if (buyerId != null) {
        return creditOrderRepository
                .findByBuyer_Id(buyerId, pageable)
                .map(CreditOrderResponse::of);
    }
    if (status != null && !status.isBlank()) {
        OrderStatus st = parseOrderStatus(status);
        return creditOrderRepository
                .findByStatus(st, pageable)
                .map(CreditOrderResponse::of);
    }
    return creditOrderRepository.findAll(pageable).map(CreditOrderResponse::of);
    }

    private OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }
}
