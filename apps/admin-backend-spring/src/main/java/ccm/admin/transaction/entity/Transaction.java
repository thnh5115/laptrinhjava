package ccm.admin.transaction.entity;

import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
/** entity - Entity - JPA entity for entity table */

public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "transaction_code", unique = true, length = 100)
    private String transactionCode;

    @Column(name = "buyer_email", nullable = true)
    private String buyerEmail;

    @Column(name = "seller_email", nullable = true)
    private String sellerEmail;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;  

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;  

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    
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
        if (type == null) {
            type = TransactionType.CREDIT_PURCHASE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
