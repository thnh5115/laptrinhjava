package ccm.cva.infrastructure.persistence.jpa;

import ccm.cva.domain.model.VerificationRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {

    boolean existsByChecksum(String checksum);

    boolean existsByOwnerIdAndTripId(UUID ownerId, String tripId);
}
