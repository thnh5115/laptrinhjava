package ccm.owner.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "carbon_credit_transactions")
public class CarbonCreditTransaction {
    // ... id, wallet, timestamp ...
    @Column(nullable = false)
    private String transactionType; // e.g., "EARNED", "TRANSFERRED", "RETIRED"

    @Column(precision = 19, scale = 4)
    private BigDecimal amount; // The amount of this transaction

    // We can link to the specific credit(s) involved if needed
    // @ManyToMany
    // private List<CarbonCredit> creditsInvolved;
}