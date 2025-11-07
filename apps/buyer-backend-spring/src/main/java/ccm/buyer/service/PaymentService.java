package ccm.buyer.service;

import ccm.buyer.entity.Payment;

public interface PaymentService {
    Payment processPayment(Long trId, String method, Double amount);
}
