package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_credits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarbonCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;

    // Admin DB dùng journey_id để liên kết nguồn gốc
    @Column(name = "journey_id")
    private Long journeyId;

    // Admin DB dùng 'amount', code cũ bạn cũng có thể dùng 'amount'
    private BigDecimal amount;

    // Admin DB có 'status' (AVAILABLE, SOLD)
    private String status;
    
    @Column(name = "price_per_credit")
    private BigDecimal pricePerCredit;

    
    
    private LocalDateTime createdAt;
}
