package ccm.buyer.service.impl;

import ccm.buyer.entity.Listing;
import ccm.buyer.enums.ListingStatus;
import ccm.buyer.repository.ListingRepository;
import ccm.buyer.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {
    private final ListingRepository listingRepository;
    @Override
        public List<Listing> getAll() {
            return listingRepository.findAll();
        }

    public void lockOrOpen(List<Listing> list) {
        for (Listing l : list) {
        BigDecimal avail = l.getAvailableQty() == null ? BigDecimal.ZERO : l.getAvailableQty();
        }
    }

    @Override
    public Listing validateOpen(Long listingId) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void reserve(Long listingId, BigDecimal qty) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void release(Long listingId, BigDecimal qty) {
        throw new UnsupportedOperationException("implement me");
    }

}
