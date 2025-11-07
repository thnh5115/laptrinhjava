package ccm.admin.journey.entity;

import ccm.admin.journey.entity.enums.JourneyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journeys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Journey - Entity - EV journey tracking for carbon credit generation
 */

public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owner of the EV journey
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Date when the journey took place
     */
    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    /**
     * Starting location of the journey
     */
    @Column(name = "start_location", length = 255)
    private String startLocation;

    /**
     * Ending location of the journey
     */
    @Column(name = "end_location", length = 255)
    private String endLocation;

    /**
     * Distance traveled in kilometers
     */
    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    /**
     * Energy consumed in kWh
     */
    @Column(name = "energy_used_kwh", precision = 10, scale = 2)
    private BigDecimal energyUsedKwh;

    /**
     * Carbon credits generated from this journey
     */
    @Column(name = "credits_generated", precision = 10, scale = 2)
    private BigDecimal creditsGenerated;

    /**
     * Verification status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JourneyStatus status;

    /**
     * CVA who verified/rejected this journey
     */
    @Column(name = "verified_by")
    private Long verifiedBy;

    /**
     * Timestamp when verification occurred
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * Reason for rejection if status is REJECTED
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Journey submission timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = JourneyStatus.PENDING;
        }
    }
}
