package ccm.admin.listing.repository;

import ccm.admin.listing.entity.Listing;
import ccm.admin.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
/** repository - Service Interface - repository business logic and data operations */

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {
    
    /**
     * Count total listings created by a specific owner
     * @param owner The owner user
     * @return Number of listings
     */
    long countByOwner(User owner);
}
