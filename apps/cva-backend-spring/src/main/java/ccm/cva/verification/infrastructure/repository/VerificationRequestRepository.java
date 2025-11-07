package ccm.cva.verification.infrastructure.repository;

import ccm.cva.verification.domain.VerificationRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID>, JpaSpecificationExecutor<VerificationRequest> {

    Optional<VerificationRequest> findByChecksum(String checksum);

    boolean existsByChecksum(String checksum);

    boolean existsByOwnerIdAndTripId(UUID ownerId, String tripId);

    @Override
    @EntityGraph(attributePaths = "creditIssuance")
    Optional<VerificationRequest> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = "creditIssuance")
    Page<VerificationRequest> findAll(Specification<VerificationRequest> spec, Pageable pageable);
}
