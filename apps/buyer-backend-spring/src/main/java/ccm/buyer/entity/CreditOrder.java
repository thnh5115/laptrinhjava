package ccm.buyer.entity;

import ccm.buyer.enums.TrStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;    
import lombok.Builder;            
import lombok.Getter;             
import lombok.NoArgsConstructor; 
import lombok.Setter;               
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(nullable = false)
    private Integer credits; 

    @Column(nullable = false)
    private Double pricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = TrStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
