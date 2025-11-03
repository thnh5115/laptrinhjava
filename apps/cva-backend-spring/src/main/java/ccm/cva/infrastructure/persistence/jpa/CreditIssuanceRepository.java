package ccm.cva.infrastructure.persistence.jpa;

import ccm.cva.domain.model.CreditIssuance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditIssuanceRepository extends JpaRepository<CreditIssuance, UUID> {

    Optional<CreditIssuance> findByIdempotencyKey(String idempotencyKey);
}
