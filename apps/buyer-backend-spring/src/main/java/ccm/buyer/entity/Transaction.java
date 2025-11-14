package ccm.buyer.entity;

import ccm.buyer.enums.TrStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long buyerId;
  
  private Long listingId;

  private BigDecimal qty;

  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private TrStatus status;

  private LocalDateTime createdAt;

  @PrePersist void onCreate(){ if(createdAt==null) createdAt = LocalDateTime.now(); }
}
