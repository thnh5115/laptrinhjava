package ccm.buyer.service;

import java.math.BigDecimal;

import ccm.buyer.entity.Bid;

public interface AuctionService {
    Bid placeBid(Long buyerId, Long auctionId, BigDecimal amount);
}
