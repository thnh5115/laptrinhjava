package ccm.owner.listing.repository;

import ccm.admin.credit.entity.CarbonCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerCreditRepository extends JpaRepository<CarbonCredit, Long> {
}
