package ccm.owner.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "carbon_credit_transactions")
public class CarbonCreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(nullable = false)
    private String transactionType; // e.g., "EARNED", "SPENT"

    // Amount for *this* transaction. Can be positive or negative.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant timestamp;

    // Nullable: A transaction might not come from a journey (e.g., manual adjustment)
    @OneToOne
    @JoinColumn(name = "source_journey_id", unique = true)
    private Journey sourceJourney;

    @PrePersist
    private void onPrePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}