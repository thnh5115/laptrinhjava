package ccm.buyer.service.impl;

import ccm.buyer.entity.Listing;
import ccm.buyer.enums.ListingStatus;
import ccm.buyer.repository.ListingRepository;
import ccm.buyer.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
      Integer avail = l.getAvailableQty() == null ? 0 : l.getAvailableQty();
      if (avail > 0) {
        l.setStatus(ListingStatus.OPEN);
      } else {
        l.setStatus(ListingStatus.LOCKED);
      }
    }
  }

  @Override
  public Listing validateOpen(Long listingId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));
    
    if (listing.getStatus() != ListingStatus.OPEN) {
      throw new IllegalStateException("Listing is not open: " + listingId);
    }
    
    return listing;
  }

  @Override
  public void reserve(Long listingId, int qty) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));
    
    Integer available = listing.getAvailableQty();
    if (available == null || available < qty) {
      throw new IllegalStateException("Not enough quantity available");
    }
    
    listing.setAvailableQty(available - qty);
    listingRepository.save(listing);
  }

  @Override
  public void release(Long listingId, int qty) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));
    
    Integer available = listing.getAvailableQty();
    listing.setAvailableQty((available == null ? 0 : available) + qty);
    listingRepository.save(listing);
  }
}
