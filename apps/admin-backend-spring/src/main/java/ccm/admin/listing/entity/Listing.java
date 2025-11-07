package ccm.admin.listing.entity;

import ccm.admin.listing.entity.enums.ListingStatus;
import ccm.admin.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Hidden
/** entity - Entity - JPA entity for entity table */

public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"role", "passwordHash", "createdAt", "updatedAt", "status"})
    private User owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Admin who approved/rejected the listing */
    @Column(name = "approved_by")
    private Long approvedBy;

    /** When the listing was approved/rejected */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** Reason for rejection or delisting */
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ListingStatus.PENDING;
        }
        if (unit == null) {
            unit = "kgCO2";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
