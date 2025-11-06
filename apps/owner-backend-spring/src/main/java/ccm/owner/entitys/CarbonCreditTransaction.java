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
@Table(name = "carbon_credit_transactions")
public class CarbonCreditTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The credit token that was moved/created/retired
    @ManyToOne(optional = false)
    @JoinColumn(name = "carbon_credit_id")
    private CarbonCredit credit;

    // The 'from' wallet. Null if it was just created (EARNED)
    @ManyToOne
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    // The 'to' wallet. Null if it was retired (RETIRED)
    @ManyToOne
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;

    @Column(nullable = false)
    private String transactionType; // "EARNED", "RETIRED", "TRANSFER"

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount; // The amount of this specific transaction

    @Column(nullable = false)
    private Instant timestamp;

    @PrePersist
    private void onPrePersist() {
        this.timestamp = Instant.now();
    }
}