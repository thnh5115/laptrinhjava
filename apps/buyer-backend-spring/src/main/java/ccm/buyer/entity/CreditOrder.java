package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String status; // e.g. PENDING, APPROVED, REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
    }
}
