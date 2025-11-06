package ccm.buyer.service.impl;

import ccm.buyer.dto.request.UpdateOrderStatusRequest;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.dto.response.CreditOrderResponse;
import ccm.buyer.entity.Buyer;
import ccm.buyer.entity.CreditOrder;
import ccm.buyer.enums.TrStatus;
import ccm.buyer.exception.NotFoundException;
import ccm.buyer.repository.BuyerRepository;
import ccm.buyer.repository.CreditOrderRepository;
import ccm.buyer.service.CreditOrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor

public class CreditOrderServiceImpl implements CreditOrderService {

    private final CreditOrderRepository orderRepo;
    private final BuyerRepository buyerRepo;

    @Override
    public java.util.List<CreditOrder> getAllOrders() {
        return orderRepo.findAll();
    }

    @Override
    public CreditOrder getOrder(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Override
    public CreditOrder createOrder(CreditOrder order) {
        if (order.getBuyer() != null && order.getBuyer().getId() != null) {
            Buyer buyer = buyerRepo.findById(order.getBuyer().getId())
                    .orElseThrow(() -> new NotFoundException("Buyer not found: " + order.getBuyer().getId()));
            order.setBuyer(buyer);
        }
        return orderRepo.save(order);
    }

    @Override
    public CreditOrder updateOrder(Long id, CreditOrder order) {
        CreditOrder exist = getOrder(id);
        if (order.getCredits() != null) exist.setCredits(order.getCredits());
        if (order.getPricePerUnit() != null) exist.setPricePerUnit(order.getPricePerUnit());
        if (order.getStatus() != null) exist.setStatus(order.getStatus());
        if (order.getBuyer() != null && order.getBuyer().getId() != null) {
            Buyer buyer = buyerRepo.findById(order.getBuyer().getId())
                    .orElseThrow(() -> new NotFoundException("Buyer not found: " + order.getBuyer().getId()));
            exist.setBuyer(buyer);
        }
        return orderRepo.save(exist);
    }

    @Override
    public CreditOrder updateOrderStatus(Long id, TrStatus status) {
        CreditOrder exist = getOrder(id);
        exist.setStatus(status);
        return orderRepo.save(exist);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepo.existsById(id)) throw new NotFoundException("Order not found: " + id);
        orderRepo.deleteById(id);
    }

    @Override
    public java.util.List<CreditOrder> getOrdersByBuyer(Long buyerId) {

        return orderRepo.findByBuyer_Id(buyerId, Pageable.unpaged()).getContent();
    }

    @Override
    public BuyerDashboardResponse getBuyerDashboard(Long buyerId) {
        long total = orderRepo.findByBuyer_Id(buyerId, Pageable.unpaged()).getTotalElements();
        long pending = orderRepo.findByStatus(TrStatus.PENDING, Pageable.unpaged()).getTotalElements();
        long completed = orderRepo.findByStatus(TrStatus.COMPLETED, Pageable.unpaged()).getTotalElements();
        double spent = 0.0;
        return BuyerDashboardResponse.builder()
                .totalOrders(total)
                .pendingTransactions(pending)
                .completedTransactions(completed)
                .totalSpent(spent)
                .build();
    }

    @Override
    public Page<CreditOrderResponse> list(Long buyerId, String status, Pageable pageable) {
        Page<CreditOrder> page;
        if (buyerId != null) {
            page = orderRepo.findByBuyer_Id(buyerId, pageable);
        } else if (status != null && !status.isBlank()) {
            TrStatus st = TrStatus.valueOf(status.toUpperCase(Locale.ROOT));
            page = orderRepo.findByStatus(st, pageable);
        } else {
            page = orderRepo.findAll(pageable);
        }
        return page.map(this::map);
    }

    @Override
    public CreditOrderResponse updateStatus(Long id, TrStatus newStatus) {
        CreditOrder o = updateOrderStatus(id, newStatus);
        return map(o);
    }

    private CreditOrderResponse map(CreditOrder o) {
        return CreditOrderResponse.builder()
                .id(o.getId())
                .buyerId(o.getBuyer() != null ? o.getBuyer().getId() : null)
                .quantity(o.getCredits() != null ? o.getCredits() : 0)
                .price(o.getPricePerUnit() != null ? o.getPricePerUnit() : 0.0)
                .status(o.getStatus())
                .build();
    }
}
