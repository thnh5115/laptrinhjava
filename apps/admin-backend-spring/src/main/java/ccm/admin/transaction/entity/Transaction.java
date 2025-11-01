package ccm.admin.transaction.entity;

import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Transaction entity for carbon credit transactions between EV Owners and Buyers
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", unique = true, nullable = false, length = 100)
    private String transactionCode;

    @Column(name = "buyer_email", nullable = false)
    private String buyerEmail;

    @Column(name = "seller_email", nullable = false)
    private String sellerEmail;

    @Column(nullable = false)
    private Double amount;  // Số lượng carbon credit

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;  // Tổng giá trị giao dịch

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Optimistic locking version field
     * Prevents concurrent modifications by detecting conflicting updates
     * 
     * PR-2 (TX-003): Protects against race conditions during status updates
     */
    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
