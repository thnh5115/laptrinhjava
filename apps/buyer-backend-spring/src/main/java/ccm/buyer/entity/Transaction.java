package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id")
    private CreditOrder order;

    private Double totalAmount;
    private String paymentMethod;
    private String transactionRef;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate(){ this.createdAt = this.updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate(){ this.updatedAt = LocalDateTime.now(); }
}
