package ccm.owner.entitys;

import ccm.owner.Journey.Journey;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
            this.wallet.setUser(this);
            this.wallet.setBalance(java.math.BigDecimal.ZERO);
        }
    }
}
