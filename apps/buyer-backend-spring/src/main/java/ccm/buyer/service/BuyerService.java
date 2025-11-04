package ccm.buyer.service;

import ccm.buyer.entity.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BuyerService {
    List<Buyer> getAllBuyers();
    Page<Buyer> getBuyers(String keyword, Pageable pageable);
    Optional<Buyer> getBuyerById(Long id);
    Buyer createBuyer(Buyer buyer);
    Buyer updateBuyer(Long id, Buyer buyer);
    void deleteBuyer(Long id);
}
