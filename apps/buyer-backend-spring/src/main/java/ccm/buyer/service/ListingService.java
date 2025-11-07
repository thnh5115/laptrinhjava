package ccm.buyer.service;

import ccm.buyer.entity.Listing;
import java.util.List;

public interface ListingService {
    List<Listing> getAll();
    Listing validateOpen(Long id);
    void reserve(Long id, int qty);
    void release(Long id, int qty);
}
