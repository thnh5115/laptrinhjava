package ccm.buyer.service;

import ccm.buyer.entity.Bid;

public interface AuctionService {
    Bid placeBid(Long buyerId, Long auctionId, Double bidPrice);
}
