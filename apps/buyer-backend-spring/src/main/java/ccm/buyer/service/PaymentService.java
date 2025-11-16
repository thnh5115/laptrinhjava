package ccm.buyer.service;

import java.math.BigDecimal;

import ccm.buyer.entity.Payment;

public interface PaymentService {
    Payment processPayment(Long trId, String method, BigDecimal amount);
}
