package ccm.owner.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // These are the sum of CarbonCredit entities
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalBalance; // Sum of AVAILABLE + LOCKED

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lockedBalance; // Sum of LOCKED

    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private EvOwner owner;

    // All credits (tokens) this wallet owns
    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY)
    private List<CarbonCredit> credits;


    @OneToMany(mappedBy = "sourceWallet", fetch = FetchType.LAZY)
    private List<CarbonCreditTransaction> outgoingTransactions;

    /**
     * All transactions where this wallet was the RECIPIENT.
     */
    @OneToMany(mappedBy = "destinationWallet", fetch = FetchType.LAZY)
    private List<CarbonCreditTransaction> incomingTransactions;

    /**
     * Helper method (not stored in the database) to calculate available funds.
     * @return The amount of credits frees to be spent or sold.
     */
    @Transient
    public BigDecimal getAvailableBalance() {
        return this.totalBalance.subtract(this.lockedBalance);
    }

    @PrePersist
    private void onPrePersist() {
        // Initialize balances to zero when a new wallet is created
        if (this.totalBalance == null) {
            this.totalBalance = BigDecimal.ZERO;
        }
        if (this.lockedBalance == null) {
            this.lockedBalance = BigDecimal.ZERO;
        }
    }
}