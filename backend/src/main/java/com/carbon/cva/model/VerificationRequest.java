package com.carbon.cva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_requests")
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "energy_used")
    private Double energyUsed;

    @Column(name = "co2_reduced")
    private Double co2Reduced;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private VerificationStatus status;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected VerificationRequest() {
        // JPA requirement
    }

    private VerificationRequest(UUID id,
                                 UUID ownerId,
                                 Double distance,
                                 Double energyUsed,
                                 Double co2Reduced,
                                 VerificationStatus status,
                                 String remark,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.distance = distance;
        this.energyUsed = energyUsed;
        this.co2Reduced = co2Reduced;
        this.status = status;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public static VerificationRequest draft(UUID ownerId,
                                             Double distance,
                                             Double energyUsed,
                                             Double co2Reduced,
                                             String remark,
                                             LocalDateTime createdAt) {
        return new VerificationRequest(null,
            ownerId,
            distance,
            energyUsed,
            co2Reduced,
            VerificationStatus.PENDING,
            remark,
            createdAt != null ? createdAt : LocalDateTime.now());
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = VerificationStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(Double energyUsed) {
        this.energyUsed = energyUsed;
    }

    public Double getCo2Reduced() {
        return co2Reduced;
    }

    public void setCo2Reduced(Double co2Reduced) {
        this.co2Reduced = co2Reduced;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
