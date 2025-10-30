package com.carbon.EvOwner.Journey;

import com.carbon.EvOwner.EvOwner;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "journeys")
public class Journey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private EvOwner owner;

    @Column(name = "start_location")
    private String startLocation;

    @Column(name = "end_location")
    private String endLocation;

    @Column(name = "distance_km", nullable = false)
    private Double distance;

    @Column(name = "energy_used_kwh", nullable = false)
    private Double energyUsed;

    @Column(name = "co2_reduced_kg")
    private Double co2Reduced;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JourneyStatus status;

    protected Journey() {
        // JPA requires no-args constructor
    }

    public Journey(EvOwner owner,
                   String startLocation,
                   String endLocation,
                   Double distance,
                   Double energyUsed) {
        this.owner = owner;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.energyUsed = energyUsed;
        this.co2Reduced = calculateCO2Reduction(distance, energyUsed);
        this.status = JourneyStatus.RECORDED;
        this.createdAt = LocalDateTime.now();
    }

    private Double calculateCO2Reduction(Double distanceKm, Double energyUsedKWh) {
        double petrolEmission = distanceKm * 0.2;
        double evEmission = distanceKm * 0.05;
        return petrolEmission - evEmission;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EvOwner getOwner() {
        return owner;
    }

    public void setOwner(EvOwner owner) {
        this.owner = owner;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public JourneyStatus getStatus() {
        return status;
    }

    public void setStatus(JourneyStatus status) {
        this.status = status;
    }
}