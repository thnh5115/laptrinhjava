package ccm.buyer.entity;

import ccm.buyer.enums.ListingStatus;
import ccm.buyer.enums.ListingType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Listing {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "buyer_id", nullable = false)
  private Buyer seller;

  @Enumerated(EnumType.STRING)
  private ListingType type; 

  private BigDecimal qty; 
  private BigDecimal availableQty;
  private BigDecimal pricePerUnit; 

  @Enumerated(EnumType.STRING)
  private ListingStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist void onCreate(){
    createdAt = LocalDateTime.now();
    updatedAt = createdAt;
    if(status==null) status = ListingStatus.DRAFT;
    if(type==null) type = ListingType.FIXED_PRICE;
    if(availableQty==null) availableQty = (qty!=null? qty:BigDecimal.ZERO);
  }
  @PreUpdate void onUpdate(){ updatedAt = LocalDateTime.now(); }

  public BigDecimal getPrice() { return pricePerUnit; }

  public BigDecimal getAvailableQty() { return availableQty; }
}
