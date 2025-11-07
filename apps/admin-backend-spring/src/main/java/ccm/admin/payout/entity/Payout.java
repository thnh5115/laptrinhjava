package ccm.admin.payout.entity;

import ccm.admin.payout.entity.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** Payout - Entity - Owner withdrawal requests managed by Admin */

public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner who requested the payout */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Amount requested for withdrawal */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** Current status of the payout request */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayoutStatus status;

    /** When the payout request was submitted */
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    /** When the payout was processed (approved/rejected) */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** Admin who processed the payout request */
    @Column(name = "processed_by")
    private Long processedBy;

    /** Admin notes or rejection reason */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Bank account details (optional, for reference) */
    @Column(name = "bank_account")
    private String bankAccount;

    /** Payment method (e.g., BANK_TRANSFER, PAYPAL) */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        if (status == null) {
            status = PayoutStatus.PENDING;
        }
    }
}
