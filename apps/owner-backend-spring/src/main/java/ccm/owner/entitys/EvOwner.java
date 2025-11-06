package ccm.owner.entitys;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TODO dummy class no implementation

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

@Entity
@Table (name = "ev-owners")
public class EvOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Wallet wallet;

    @PrePersist
    private void initWallet() {
        if (this.wallet == null) {
            this.wallet = new Wallet();
            this.wallet.setOwner(this);
            this.wallet.setBalance(java.math.BigDecimal.ZERO);
        }
    }
}
