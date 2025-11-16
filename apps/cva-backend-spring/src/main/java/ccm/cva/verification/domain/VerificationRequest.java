package ccm.cva.verification.domain;

import ccm.cva.issuance.domain.CreditIssuance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "verification_requests",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_verification_requests_checksum", columnNames = {"checksum"}),
        @UniqueConstraint(name = "uk_verification_requests_owner_trip", columnNames = {"owner_id", "trip_id"})
    },
    indexes = {
        @Index(name = "idx_verification_requests_owner", columnList = "owner_id"),
        @Index(name = "idx_verification_requests_status", columnList = "status"),
        @Index(name = "idx_verification_requests_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID ownerId;

    @Column(name = "trip_id", nullable = false, length = 100)
    private String tripId;

    @Column(name = "distance_km", nullable = false, precision = 12, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "energy_kwh", nullable = false, precision = 12, scale = 3)
    private BigDecimal energyKwh;

    @Column(name = "checksum", nullable = false, length = 128, unique = true)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verifier_id")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID verifierId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "verificationRequest", fetch = FetchType.LAZY)
    private CreditIssuance creditIssuance;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = VerificationStatus.PENDING;
        }
    }
}
