package ccm.buyer.service.impl;

import ccm.buyer.entity.Auction;
import ccm.buyer.entity.Bid;
import ccm.buyer.entity.Buyer;
import ccm.buyer.enums.BidStatus;
import ccm.buyer.repository.AuctionRepository;
import ccm.buyer.repository.BidRepository;
import ccm.buyer.repository.BuyerRepository;
import ccm.buyer.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

  private final AuctionRepository auctionRepository;
  private final BidRepository bidRepository;
  private final BuyerRepository buyerRepository;

  @Override
  public Bid placeBid(Long buyerId, Long auctionId, Double bidPrice) {
    // Validate auction exists and is active
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
    
    // Validate buyer exists
    Buyer buyer = buyerRepository.findById(buyerId)
        .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + buyerId));
    
    // Validate bid price meets minimum requirements
    Double minBidPrice = auction.getStartPrice();
    Double stepPrice = auction.getStepPrice();
    if (stepPrice != null && minBidPrice != null) {
      minBidPrice += stepPrice;
    }
    
    if (minBidPrice != null && bidPrice < minBidPrice) {
      throw new IllegalArgumentException("Bid price too low. Minimum: " + minBidPrice);
    }
    
    // Create and save bid
    Bid bid = Bid.builder()
        .auction(auction)
        .buyer(buyer)
        .amount(bidPrice)
        .status(BidStatus.OPEN)
        .createdAt(LocalDateTime.now())
        .build();
    
    return bidRepository.save(bid);
  }
}
