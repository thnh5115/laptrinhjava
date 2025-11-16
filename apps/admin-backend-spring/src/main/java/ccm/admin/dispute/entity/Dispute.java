package ccm.admin.dispute.entity;

import ccm.admin.dispute.entity.enums.DisputeStatus;
import ccm.admin.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
/** entity - Entity - JPA entity for entity table */

public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false, unique = true, length = 50)
    private String disputeCode;

    
    @Column(nullable = false)
    private Long transactionId;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by_user_id", nullable = false)
    @JsonIgnoreProperties({"role", "passwordHash", "createdAt", "updatedAt", "status"})
    private User raisedByUser;

    
    @Column(columnDefinition = "TEXT")
    private String description;

    
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;

    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DisputeStatus.OPEN;
        }
    }

    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
