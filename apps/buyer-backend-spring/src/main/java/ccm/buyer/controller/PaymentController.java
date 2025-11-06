package ccm.buyer.controller;

import ccm.buyer.dto.response.PaymentResponse;
import ccm.buyer.entity.Payment;
import ccm.buyer.enums.PayStatus;
import ccm.buyer.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentRepository paymentRepository;

  @GetMapping
  public ResponseEntity<List<PaymentResponse>> list() {
    List<Payment> data = paymentRepository.findAll();
    return ResponseEntity.ok(
        data.stream().map(p -> PaymentResponse.builder()
            .id(p.getId())
            .trId(p.getTrId())
            .method(p.getMethod())
            .ref(p.getRef())
            .amount(p.getAmount())
            .status(p.getStatus())
            .createdAt(p.getCreatedAt())
            .build()
        ).toList()
    );
  }

  @PutMapping("/{id}/success")
  public ResponseEntity<PaymentResponse> markSuccess(@PathVariable Long id) {
    Payment p = paymentRepository.findById(id).orElseThrow();
    p.setStatus(PayStatus.SUCCESS);
    p = paymentRepository.save(p);
    return ResponseEntity.ok(PaymentResponse.builder()
        .id(p.getId())
        .trId(p.getTrId())
        .method(p.getMethod())
        .ref(p.getRef())
        .amount(p.getAmount())
        .status(p.getStatus())
        .createdAt(p.getCreatedAt())
        .build());
  }
}
