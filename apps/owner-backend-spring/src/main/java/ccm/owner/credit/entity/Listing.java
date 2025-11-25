package ccm.owner.credit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "OwnerCreditListing")
@Table(name = "listings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Listing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "carbon_credit_id")
    private CarbonCredit carbonCredit;

    @Column(name = "seller_id")
    private Long sellerId;

    private BigDecimal quantity;
    private BigDecimal price; // price_per_unit
    private String status;    // PENDING, APPROVED...
    
    @Column(name = "listing_type")
    private String type;      // FIXED_PRICE

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
