package ccm.cva.issuance.domain;

import ccm.cva.verification.domain.VerificationRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Dùng LocalDateTime cho đồng bộ

@Entity
@Table(
    name = "credit_issuances",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_credit_issuances_request", columnNames = {"request_id"}),
        @UniqueConstraint(name = "uk_credit_issuances_idempotency", columnNames = {"idempotency_key"})
    },
    indexes = {
        @Index(name = "idx_credit_issuances_owner", columnList = "owner_id"),
        @Index(name = "idx_credit_issuances_created_at", columnList = "created_at"),
        @Index(name = "idx_credit_issuances_corr", columnList = "correlation_id")
    }
)
@Getter
@Setter
public class CreditIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SỬA: Dùng ID tự tăng (Long)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private VerificationRequest verificationRequest; // Bây giờ nó sẽ link tới VerificationRequest (Long ID)

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; 

    @Column(name = "co2_reduced_kg", nullable = false, precision = 18, scale = 6)
    private BigDecimal co2ReducedKg;

    @Column(name = "credits_raw", nullable = false, precision = 18, scale = 6)
    private BigDecimal creditsRaw;

    @Column(name = "credits_rounded", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditsRounded;

    @Column(name = "idempotency_key", nullable = false, length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // SỬA: Instant -> LocalDateTime

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}