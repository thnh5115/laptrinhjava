package ccm.buyer.service.impl;

import ccm.buyer.entity.Listing;
import ccm.buyer.enums.ListingStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListingServiceImpl {

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

  public Listing validateOpen(Long listingId) {
    throw new UnsupportedOperationException("implement me");
  }

  public void reserve(Long listingId, Integer qty) {
    throw new UnsupportedOperationException("implement me");
  }

  public void release(Long listingId, Integer qty) {
    throw new UnsupportedOperationException("implement me");
  }
}
