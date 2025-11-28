package ccm.owner.credit.repository;
import ccm.owner.credit.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerListingCreditRepository extends JpaRepository<Listing, Long> {}