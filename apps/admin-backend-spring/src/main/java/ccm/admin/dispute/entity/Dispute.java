package ccm.admin.dispute.entity;

import ccm.admin.dispute.entity.enums.DisputeStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a dispute/complaint raised by Buyer or EV Owner
 * Related to a transaction for easy traceability
 */
@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique dispute code (e.g., DIS-2025-0001)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String disputeCode;

    /**
     * Related transaction ID
     */
    @Column(nullable = false)
    private Long transactionId;

    /**
     * Email of the person who raised the dispute (Buyer or EV Owner)
     */
    @Column(nullable = false)
    private String raisedBy;

    /**
     * Description of the dispute
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Admin's note/response to the dispute
     */
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    /**
     * Current status of the dispute
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;

    /**
     * When the dispute was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the dispute was last updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically set timestamps before persist
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DisputeStatus.OPEN;
        }
    }

    /**
     * Automatically update timestamp before update
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
