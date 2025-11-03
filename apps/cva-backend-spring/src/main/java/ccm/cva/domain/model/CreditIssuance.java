package ccm.cva.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_issuances",
        indexes = {
                @Index(name = "idx_credit_issuances_owner", columnList = "owner_id"),
                @Index(name = "idx_credit_issuances_request", columnList = "request_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_credit_issuances_idempotency", columnNames = {"idempotency_key"})
        })
public class CreditIssuance {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, columnDefinition = "BINARY(16)")
    private VerificationRequest request;

    @Column(name = "owner_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID ownerId;

    @Column(name = "co2_reduced_kg", nullable = false, precision = 16, scale = 6)
    private BigDecimal co2ReducedKg;

    @Column(name = "credits_raw", nullable = false, precision = 18, scale = 6)
    private BigDecimal creditsRaw;

    @Column(name = "credits_rounded", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditsRounded;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
