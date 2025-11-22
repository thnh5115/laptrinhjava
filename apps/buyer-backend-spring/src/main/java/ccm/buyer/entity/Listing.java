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

  // 1. THÊM MỚI: Liên kết với CarbonCredit (Bắt buộc theo DB)
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "carbon_credit_id", nullable = false)
  private CarbonCredit carbonCredit;

  // 2. SỬA: Đổi từ buyer_id thành seller_id (Người bán mới tạo Listing)
  // Buyer ở đây thực chất là entity User mà ta đã map lại
  @JoinColumn(name = "seller_id", nullable = false) 
   private Long sellerId;

  // 3. SỬA: Map enum type sang cột listing_type
  @Enumerated(EnumType.STRING)
  @Column(name = "listing_type", nullable = false)
  private ListingType type; 

  // 4. SỬA: Map qty sang cột quantity
  @Column(name = "quantity", nullable = false)
  private BigDecimal qty; 

  // 5. XỬ LÝ: Cột này không có trong DB, dùng @Transient để Hibernate bỏ qua
  // Bạn sẽ cần tự tính toán giá trị này trong Service nếu cần
  @Transient
  private BigDecimal availableQty;

  // 6. SỬA: Map pricePerUnit sang cột price
  @Column(name = "price", nullable = false)
  private BigDecimal pricePerUnit; 

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ListingStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist void onCreate(){
    createdAt = LocalDateTime.now();
    updatedAt = createdAt;
    if(status==null) status = ListingStatus.DRAFT;
    if(type==null) type = ListingType.FIXED_PRICE;
    
    // Logic tạm: Nếu không có availableQty thì lấy bằng qty ban đầu
    if(availableQty==null) availableQty = (qty!=null ? qty : BigDecimal.ZERO);
  }
  
  @PreUpdate void onUpdate(){ updatedAt = LocalDateTime.now(); }

  public BigDecimal getPrice() { return pricePerUnit; }
  public BigDecimal getAvailableQty() { return availableQty; }
}