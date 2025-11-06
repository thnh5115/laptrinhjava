package ccm.buyer.service;

import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.dto.response.CreditOrderResponse;
import ccm.buyer.entity.CreditOrder;
import ccm.buyer.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CreditOrderService {

    List<CreditOrder> getAllOrders();

    CreditOrder getOrderById(Long id);

    CreditOrder createOrder(CreditOrder order);

    CreditOrder updateOrder(Long id, CreditOrder order);

    CreditOrder updateOrderStatus(Long id, OrderStatus status);

    void deleteOrder(Long id);

    List<CreditOrder> getOrdersByBuyer(Long buyerId);

    BuyerDashboardResponse getBuyerDashboard(Long buyerId);

    Page<CreditOrderResponse> list(Long buyerId, String status, Pageable pageable);

    CreditOrderResponse updateStatus(Long id, OrderStatus newStatus);
}
