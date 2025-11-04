package ccm.cva.domain.model;

import ccm.cva.domain.model.enums.VerificationRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "verification_requests",
        indexes = {
                @Index(name = "idx_verification_requests_owner", columnList = "owner_id"),
                @Index(name = "idx_verification_requests_status", columnList = "status"),
                @Index(name = "idx_verification_requests_created_at", columnList = "created_at")
        },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_verification_requests_checksum", columnNames = {"checksum"}),
        @UniqueConstraint(name = "uk_verification_requests_owner_trip", columnNames = {"owner_id", "trip_id"})
    })
public class VerificationRequest {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "owner_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID ownerId;

    @Column(name = "trip_id", nullable = false, length = 128)
    private String tripId;

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "energy_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal energyKwh;

    @Column(name = "checksum", nullable = false, length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private VerificationRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "verifier_id", columnDefinition = "BINARY(16)")
    private UUID verifierId;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
    private CreditIssuance issuance;

    public boolean isPending() {
        return VerificationRequestStatus.PENDING.equals(status);
    }

    public void markApproved(UUID verifier, OffsetDateTime timestamp, String approvalNotes) {
        this.status = VerificationRequestStatus.APPROVED;
        this.verifierId = verifier;
        this.verifiedAt = timestamp;
        this.notes = approvalNotes;
    }

    public void markRejected(UUID verifier, OffsetDateTime timestamp, String rejectionNotes) {
        this.status = VerificationRequestStatus.REJECTED;
        this.verifierId = verifier;
        this.verifiedAt = timestamp;
        this.notes = rejectionNotes;
    }

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (status == null) {
            status = VerificationRequestStatus.PENDING;
        }
    }
}
