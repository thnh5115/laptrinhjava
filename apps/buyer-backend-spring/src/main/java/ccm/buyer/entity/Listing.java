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

  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "carbon_credit_id")
  private Long carbonCreditId;
  
  // Map với cột seller_id trong DB
  @Column(name = "seller_id", nullable = false) 
  private Long sellerId;

  // Map với cột quantity trong DB
  @Column(name = "quantity", nullable = false)
  private BigDecimal qty;

  // --- ĐÂY LÀ CHỖ SỬA QUAN TRỌNG NHẤT ---
  // Map với cột price trong DB (thay vì mặc định là price_per_credit)
  @Column(name = "price", nullable = false)
  private BigDecimal pricePerUnit;
  // --------------------------------------

  @Enumerated(EnumType.STRING)
  @Column(name = "listing_type", nullable = false)
  private ListingType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ListingStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  
  @Transient
  private BigDecimal availableQty;

  // Getter tương thích
  public BigDecimal getPrice() { return pricePerUnit; }
  public BigDecimal getAvailableQty() { return qty; }

  @PrePersist void onCreate(){
    createdAt = LocalDateTime.now();
    updatedAt = createdAt;
    if(status==null) status = ListingStatus.PENDING; // Mặc định Pending nếu null
  }
  
  @PreUpdate void onUpdate(){ updatedAt = LocalDateTime.now(); }
}