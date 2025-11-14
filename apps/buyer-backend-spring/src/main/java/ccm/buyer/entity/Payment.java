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

  @Column(name = "tr_id", nullable = false)
  private Long trId;

  private String method;
  private String ref; 
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private PayStatus status;

  private LocalDateTime createdAt;

  @PrePersist void onCreate(){ if(createdAt==null) createdAt = LocalDateTime.now(); }
}
