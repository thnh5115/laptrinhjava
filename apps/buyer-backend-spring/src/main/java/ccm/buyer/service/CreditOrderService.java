package ccm.buyer.service;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.entity.OrderStatus;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import java.util.List;

public interface CreditOrderService {
    List<CreditOrder> getAllOrders();
    List<CreditOrder> getOrdersByBuyer(Long buyerId);
    CreditOrder getOrderById(Long id);
    CreditOrder createOrder(CreditOrder order);
    CreditOrder updateOrder(Long id, CreditOrder order);
    CreditOrder updateOrderStatus(Long id, OrderStatus status);
    void deleteOrder(Long id);
    BuyerDashboardResponse getBuyerDashboard(Long buyerId);
}
