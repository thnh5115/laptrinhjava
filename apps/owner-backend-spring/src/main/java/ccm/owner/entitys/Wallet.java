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

    // --- CACHED BALANCES ---
    // These are the sum of CarbonCredit entities
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalBalance; // Sum of AVAILABLE + LOCKED

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lockedBalance; // Sum of LOCKED
    // ---

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // All credits (tokens) this wallet owns
    @OneToMany(mappedBy = "wallet")
    private List<CarbonCredit> credits;

    // All transactions (history log)
    @OneToMany(mappedBy = "wallet")
    private List<CarbonCreditTransaction> transactions;

    // Helper method (not in DB)
    @Transient
    public BigDecimal getAvailableBalance() {
        return this.totalBalance.subtract(this.lockedBalance);
    }

    @PrePersist
    private void onPrePersist() {
        this.totalBalance = BigDecimal.ZERO;
        this.lockedBalance = BigDecimal.ZERO;
    }
}