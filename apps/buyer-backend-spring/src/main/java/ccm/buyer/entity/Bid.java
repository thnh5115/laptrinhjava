package ccm.buyer.entity;

import ccm.buyer.enums.BidStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Bid {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "auction_id", nullable = false)
  private Auction auction;

  
  @JoinColumn(name = "buyer_id", nullable = false)
  private Long buyerId;

 @Column(name = "bid_price", nullable = false) // <--- Ánh xạ biến 'amount' vào cột 'bid_price'
    private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private BidStatus status;

  private LocalDateTime createdAt;

  @PrePersist void onCreate(){ if(createdAt==null) createdAt = LocalDateTime.now(); }
}
