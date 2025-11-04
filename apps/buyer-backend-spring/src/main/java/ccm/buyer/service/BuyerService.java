package ccm.buyer.service;

import ccm.buyer.entity.Buyer;
import java.util.List;
import java.util.Optional;

public interface BuyerService {
    List<Buyer> getAllBuyers();
    Optional<Buyer> getBuyerById(Long id);
    Buyer createBuyer(Buyer buyer);
    Buyer updateBuyer(Long id, Buyer buyer);
    void deleteBuyer(Long id);
}
