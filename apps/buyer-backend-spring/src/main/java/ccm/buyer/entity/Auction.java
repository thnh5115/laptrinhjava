package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "auctions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Auction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private Listing listing;

  @Column(name = "start_price", nullable = false)
  private BigDecimal startPrice;

  @Column(name = "step_price", nullable = false)
  private BigDecimal stepPrice;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;
  
}
