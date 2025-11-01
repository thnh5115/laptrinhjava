package ccm.admin.dispute.repository;

import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.entity.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for Dispute entity
 * Supports dynamic filtering via JpaSpecificationExecutor
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long>,
        JpaSpecificationExecutor<Dispute> {
    
    /**
     * Count disputes by status
     * Useful for dashboard statistics
     */
    long countByStatus(DisputeStatus status);
}
