package ccm.owner.entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;


@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "carbon_credits")
public class CarbonCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    // The original source
    @OneToOne
    @JoinColumn(name = "source_journey_id")
    private Journey sourceJourney;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    // Could add more for a real marketplace:
    // private String vintage; // e.g., "2024"
    // private String projectType; // e.g., "RENEWABLE_ENERGY"

    @PrePersist
    private void onPrePersist() {
        this.createdAt = Instant.now();
        this.status = CreditStatus.AVAILABLE;
    }
}