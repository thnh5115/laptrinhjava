package ccm.buyer.service.impl;

import ccm.buyer.entity.Auction;
import ccm.buyer.entity.Bid;
import ccm.buyer.entity.Buyer;
import ccm.buyer.enums.BidStatus;
import ccm.buyer.repository.BidRepository;
import ccm.buyer.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl {

  private final BidRepository bidRepository;
  private final BuyerRepository buyerRepository;

  public Bid placeBid(Auction auction, Long buyerId, Double amount) {

    Double minNext = (auction.getStepPrice() == null ? 0.0 : auction.getStepPrice());

    Buyer buyer = buyerRepository.findById(buyerId).orElseThrow();
    Bid bid = Bid.builder()
        .auction(auction)
        .buyer(buyer)
        .amount(amount)
        .status(BidStatus.OPEN)
        .createdAt(LocalDateTime.now())
        .build();
    return bidRepository.save(bid);
  }
}
