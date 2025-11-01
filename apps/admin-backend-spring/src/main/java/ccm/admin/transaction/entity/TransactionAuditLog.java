package ccm.admin.transaction.entity;

import ccm.admin.transaction.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PR-2 (TX-004): Audit trail for transaction status changes
 * Tracks who changed transaction status, when, and what changed
 */
@Entity
@Table(name = "transaction_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "transaction_code", nullable = false, length = 100)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private TransactionStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private TransactionStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy; // Email of admin who made the change

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "reason", length = 500)
    private String reason; // Optional reason for status change

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
