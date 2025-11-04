package ccm.buyer.service;

import ccm.buyer.entity.CreditOrder;
import java.util.List;

public interface CreditOrderService {
    List<CreditOrder> getOrdersByBuyer(Long buyerId);
    CreditOrder createOrder(CreditOrder order);
    CreditOrder updateStatus(Long id, String status);
}
