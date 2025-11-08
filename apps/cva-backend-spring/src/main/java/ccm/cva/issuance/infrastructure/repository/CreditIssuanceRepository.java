package ccm.cva.issuance.infrastructure.repository;

import ccm.cva.issuance.domain.CreditIssuance;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditIssuanceRepository extends JpaRepository<CreditIssuance, UUID>, JpaSpecificationExecutor<CreditIssuance> {

    Optional<CreditIssuance> findByIdempotencyKey(String idempotencyKey);

    Optional<CreditIssuance> findByVerificationRequest_Id(UUID requestId);

    @Query("select coalesce(sum(ci.creditsRounded), 0) from CreditIssuance ci")
    BigDecimal sumCreditsRounded();

    List<CreditIssuance> findAllByCreatedAtBetweenOrderByCreatedAtAsc(Instant from, Instant to);
}
