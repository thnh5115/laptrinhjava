package ccm.buyer.service;

import ccm.buyer.entity.Listing;

import java.math.BigDecimal;
import java.util.List;

public interface ListingService {
    List<Listing> getAll();
    Listing validateOpen(Long id);
    void reserve(Long id, BigDecimal qty);
    void release(Long id, BigDecimal qty);
}
