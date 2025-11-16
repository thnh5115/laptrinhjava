package ccm.admin.dispute.repository;

import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.entity.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/** repository - Service Interface - repository business logic and data operations */

public interface DisputeRepository extends JpaRepository<Dispute, Long>,
        JpaSpecificationExecutor<Dispute> {
    
    
    long countByStatus(DisputeStatus status);
    
    /**
     * Count disputes raised by a specific user
     * @param userEmail The user email
     * @return Number of disputes
     */
    @Query("SELECT COUNT(d) FROM Dispute d WHERE LOWER(d.raisedByUser.email) = LOWER(:userEmail)")
    long countByUserEmail(@Param("userEmail") String userEmail);
}
