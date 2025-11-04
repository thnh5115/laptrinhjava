package ccm.buyer.service.impl;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.entity.OrderStatus;
import ccm.buyer.repository.CreditOrderRepository;
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
    public List<CreditOrder> getOrdersByBuyer(Long buyerId) {
        return creditOrderRepository.findByBuyerId(buyerId);
    }

    @Override
    public CreditOrder createOrder(CreditOrder order) {
        order.setStatus(OrderStatus.PENDING);
        return creditOrderRepository.save(order);
    }

    @Override
    public CreditOrder updateStatus(Long id, String status) {
        return creditOrderRepository.findById(id)
                .map(o -> {
                    o.setStatus(OrderStatus.valueOf(status));
                    return creditOrderRepository.save(o);
                })
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}
