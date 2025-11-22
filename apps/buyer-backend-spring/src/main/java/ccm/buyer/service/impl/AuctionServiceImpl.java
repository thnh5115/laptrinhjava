package ccm.buyer.service.impl;

import ccm.buyer.entity.Auction;
import ccm.buyer.entity.Bid;

import ccm.buyer.enums.BidStatus;
import ccm.buyer.repository.AuctionRepository;
import ccm.buyer.repository.BidRepository;

import ccm.buyer.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor

@Transactional

public class AuctionServiceImpl implements AuctionService {

  private final AuctionRepository auctionRepository;
  private final BidRepository bidRepository;
  

  @Override
  public Bid placeBid(Long auctionId, Long buyerId, BigDecimal amount) {

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Bid amount must be > 0");
    }

    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new RuntimeException("Auction not found"));
    

    Bid bid = Bid.builder()
    .auction(auction)
    .buyerId(buyerId) // Dùng thẳng ID truyền vào
    .amount(amount) // Nhớ là bạn đã map cột này là 'bid_price' ở bước trước
    .status(BidStatus.OPEN)
    .build();
    
    return bidRepository.save(bid);
  }
}
