package ccm.buyer.repository;

import ccm.buyer.entity.Listing;
import ccm.buyer.enums.ListingStatus;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByStatusOrderByPricePerUnitAsc(ListingStatus status);
}
