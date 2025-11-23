package ccm.cva.verification.infrastructure.repository;

import ccm.cva.verification.domain.VerificationRequest;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long>, JpaSpecificationExecutor<VerificationRequest> {

    Optional<VerificationRequest> findByChecksum(String checksum);

    boolean existsByChecksum(String checksum);

    //boolean existsByOwnerIdAndTripId(Long ownerId, String tripId);

    @Override
    @EntityGraph(attributePaths = "creditIssuance")
    Optional<VerificationRequest> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "creditIssuance")
    Page<VerificationRequest> findAll(Specification<VerificationRequest> spec, Pageable pageable);
}
