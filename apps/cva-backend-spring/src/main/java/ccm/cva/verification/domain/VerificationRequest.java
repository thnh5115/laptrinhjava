package ccm.cva.verification.domain;

import ccm.cva.issuance.domain.CreditIssuance; // 1. Import class này
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "journeys") // Map vào bảng journeys của Admin
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long ownerId;

    @Transient 
    private String tripId; 
    
    public String getTripId() {
        return tripId != null ? tripId : "TRIP-" + id;
    }

    @Column(name = "distance_km")
    private BigDecimal distanceKm;

    @Column(name = "energy_used_kwh")
    private BigDecimal energyKwh;

    @Column(name = "rejection_reason", insertable = false, updatable = false) 
    private String checksum; 

    @Column(name = "status")
    private String statusString;

    public VerificationStatus getStatus() {
        try {
            if ("VERIFIED".equalsIgnoreCase(statusString)) return VerificationStatus.APPROVED;
            if ("REJECTED".equalsIgnoreCase(statusString)) return VerificationStatus.REJECTED;
            return VerificationStatus.PENDING;
        } catch (Exception e) {
            return VerificationStatus.PENDING;
        }
    }

    public void setStatus(VerificationStatus status) {
        if (status == VerificationStatus.APPROVED) this.statusString = "VERIFIED";
        else if (status == VerificationStatus.REJECTED) this.statusString = "REJECTED";
        else this.statusString = "PENDING";
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifierId;

    @Column(name = "rejection_reason")
    private String notes;

    // 2. KHÔI PHỤC TRƯỜNG NÀY (Để sửa lỗi getCreditIssuance)
    // mappedBy = "verificationRequest" nghĩa là bảng credit_issuances nắm giữ khóa ngoại
    @OneToOne(mappedBy = "verificationRequest", fetch = FetchType.LAZY)
    private CreditIssuance creditIssuance;
}