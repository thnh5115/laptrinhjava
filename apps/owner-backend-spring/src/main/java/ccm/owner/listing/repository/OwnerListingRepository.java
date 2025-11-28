package ccm.owner.listing.repository;

// SỬA IMPORT: Dùng Listing của Owner vừa tạo
import ccm.owner.listing.entity.Listing; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface OwnerListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {
}