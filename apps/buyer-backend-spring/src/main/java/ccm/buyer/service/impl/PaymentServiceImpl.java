package ccm.buyer.service.impl;

import ccm.buyer.enums.PayStatus;
import ccm.buyer.entity.Payment;
import ccm.buyer.repository.PaymentRepository;
import ccm.buyer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repo;

    @Override
    @Transactional
    public Payment processPayment(Long trId, String method, Double amount) {
        String ref = "PMT-" + UUID.randomUUID();

        if (repo.existsByRef(ref))
            throw new IllegalStateException("Duplicate payment reference detected");

        Payment payment = Payment.builder()
                .trId(trId)
                .method(method)
                .amount(amount)
                .ref(ref)
                .status(PayStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        return repo.save(payment);
    }
}
