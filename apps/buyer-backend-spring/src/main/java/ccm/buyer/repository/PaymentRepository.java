package ccm.buyer.repository;

import ccm.buyer.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByRef(String ref);
}
