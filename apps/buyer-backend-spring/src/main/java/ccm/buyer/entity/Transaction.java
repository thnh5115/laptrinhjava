package ccm.buyer.entity;

import ccm.buyer.enums.TrStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions") // Khớp với bảng của Admin
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", unique = true) // Thêm dòng này nếu muốn khớp DB Admin
    private String transactionCode;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
  
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    // Lưu ý: Admin dùng "quantity", code bạn dùng "qty". 
    // Sửa tên biến hoặc dùng @Column để map.
    @Column(name = "quantity", nullable = false) 
    private BigDecimal qty; 

    @Column(name = "total_amount", nullable = false) // Admin dùng "total_amount"
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TrStatus status;

    // Admin có cột "type"
    @Column(name = "type")
    private String type; // hoặc Enum TrType

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // Thêm nếu cần

    @PrePersist void onCreate(){ 
        if(createdAt==null) createdAt = LocalDateTime.now(); 
        if(type==null) type = "CREDIT_PURCHASE"; // Mặc định
    }
}