package ccm.buyer.entity;

import ccm.buyer.enums.TrStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", unique = true)
    private String transactionCode;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "quantity", nullable = false)
    private BigDecimal qty;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TrStatus status;

    @Column(name = "type")
    private String type;

    private LocalDateTime createdAt;
    
    // Sửa ở đây: Đảm bảo updatedAt không bị null
    private LocalDateTime updatedAt;

    // --- SỬA ĐOẠN NÀY ---
    @PrePersist 
    void onCreate(){ 
        if(createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // <--- THÊM DÒNG NÀY ĐỂ KHẮC PHỤC LỖI
        if(type == null) type = "CREDIT_PURCHASE";
        
        // Tự sinh mã giao dịch nếu chưa có (để tránh lỗi null transaction_code nếu có unique constraint)
        if(transactionCode == null) {
            transactionCode = "TRX-" + System.currentTimeMillis(); 
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now(); // Cập nhật thời gian khi sửa đổi
    }
}