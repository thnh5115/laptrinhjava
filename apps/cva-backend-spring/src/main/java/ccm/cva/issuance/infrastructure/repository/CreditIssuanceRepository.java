package ccm.cva.issuance.infrastructure.repository;

import ccm.cva.issuance.domain.CreditIssuance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditIssuanceRepository extends JpaRepository<CreditIssuance, UUID> {

    Optional<CreditIssuance> findByIdempotencyKey(String idempotencyKey);

    Optional<CreditIssuance> findByVerificationRequest_Id(UUID requestId);
}
