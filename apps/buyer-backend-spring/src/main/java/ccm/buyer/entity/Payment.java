package ccm.buyer.entity;

import ccm.buyer.enums.PayStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_id", nullable = false, unique = true) // Map trId -> transaction_id
  private Long trId;

  private String method;

  @Column(name = "payment_gateway_ref") // Map ref -> payment_gateway_ref
  private String ref; 
  
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private PayStatus status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist void onCreate(){ if(createdAt==null) createdAt = LocalDateTime.now(); }
}