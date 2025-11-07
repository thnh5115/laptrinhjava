package ccm.admin.credit.entity;

import ccm.admin.credit.entity.enums.CreditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_credits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** CarbonCredit - Entity - Carbon credits generated from verified EV journeys */

public class CarbonCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of the carbon credit (EV owner) */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** Journey that generated this credit */
    @Column(name = "journey_id", nullable = false, unique = true)
    private Long journeyId;

    /** Amount of carbon credits (in tons CO2) */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** Current status of the credit */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditStatus status;

    /** Price per credit when listed */
    @Column(name = "price_per_credit", precision = 10, scale = 2)
    private BigDecimal pricePerCredit;

    /** When the credit was listed on marketplace */
    @Column(name = "listed_at")
    private LocalDateTime listedAt;

    /** When the credit was sold */
    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    /** Buyer who purchased the credit */
    @Column(name = "buyer_id")
    private Long buyerId;

    /** Credit generation timestamp */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = CreditStatus.AVAILABLE;
        }
    }
}
