package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "e_wallets") // Map vào bảng ví chung
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // Đây chính là sellerId

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
