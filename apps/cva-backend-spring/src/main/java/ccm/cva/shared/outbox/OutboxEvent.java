package ccm.cva.shared.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "cva_outbox",
    indexes = {
        @Index(name = "idx_outbox_status", columnList = "status"),
        @Index(name = "idx_outbox_next_attempt", columnList = "next_attempt_at")
    }
)
@Getter
@Setter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private OutboxEventType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public void incrementAttempts(String errorMessage, Instant nextAttemptTimestamp) {
        this.attempts = this.attempts + 1;
        this.lastError = errorMessage;
        this.nextAttemptAt = nextAttemptTimestamp;
        this.status = OutboxStatus.PENDING;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.lastError = null;
        this.nextAttemptAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.lastError = errorMessage;
        this.nextAttemptAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.nextAttemptAt == null) {
            this.nextAttemptAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
