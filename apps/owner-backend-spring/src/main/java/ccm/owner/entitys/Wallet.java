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
    @JoinColumn(name = "owner_id", unique = true)
    private EvOwner owner;

    // All credits (tokens) this wallet owns
    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY)
    private List<CarbonCredit> credits;

    // All transactions (history log)
    @OneToMany(mappedBy = "wallet")
    private List<CarbonCreditTransaction> transactions;

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