package ccm.cva.verification.infrastructure.repository;

import ccm.cva.verification.domain.VerificationRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {

    Optional<VerificationRequest> findByChecksum(String checksum);
}
