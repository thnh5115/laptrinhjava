package ccm.cva.application.service.impl;

import ccm.cva.application.service.ValidationEngine;
import ccm.cva.application.service.dto.CreateVerificationRequestCommand;
import ccm.cva.domain.exception.DuplicateTripVerificationRequestException;
import ccm.cva.domain.exception.DuplicateVerificationRequestException;
import ccm.cva.domain.exception.InvalidTripMetricsException;
import ccm.cva.infrastructure.persistence.jpa.VerificationRequestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultValidationEngine implements ValidationEngine {

    private static final int EFFICIENCY_SCALE = 4;
    // Acceptable efficiency envelope for electric trips, expressed in kWh per km.
    private static final BigDecimal MIN_EFFICIENCY_KWH_PER_KM = new BigDecimal("0.05");
    private static final BigDecimal MAX_EFFICIENCY_KWH_PER_KM = new BigDecimal("0.35");

    private final VerificationRequestRepository verificationRequestRepository;

    @Override
    public void validateNewRequest(CreateVerificationRequestCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateVerificationRequestCommand must not be null");
        }
        if (command.checksum() == null || command.checksum().isBlank()) {
            throw new IllegalArgumentException("Checksum must not be blank");
        }
        if (verificationRequestRepository.existsByChecksum(command.checksum())) {
            throw new DuplicateVerificationRequestException(command.checksum());
        }

        Objects.requireNonNull(command.ownerId(), "OwnerId must not be null");
        if (command.tripId() == null || command.tripId().isBlank()) {
            throw new IllegalArgumentException("TripId must not be blank");
        }
        if (verificationRequestRepository.existsByOwnerIdAndTripId(command.ownerId(), command.tripId())) {
            throw new DuplicateTripVerificationRequestException(command.ownerId(), command.tripId());
        }

        validatePositive("distanceKm", command.distanceKm());
        validatePositive("energyKwh", command.energyKwh());

        validateTripMetrics(command.distanceKm(), command.energyKwh());
    }

    private void validatePositive(String field, BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be a positive number");
        }
    }

    private void validateTripMetrics(BigDecimal distanceKm, BigDecimal energyKwh) {
        BigDecimal efficiency = energyKwh.divide(distanceKm, EFFICIENCY_SCALE, RoundingMode.HALF_UP);
        if (efficiency.compareTo(MIN_EFFICIENCY_KWH_PER_KM) < 0
                || efficiency.compareTo(MAX_EFFICIENCY_KWH_PER_KM) > 0) {
            String formattedEfficiency = efficiency.setScale(EFFICIENCY_SCALE, RoundingMode.HALF_UP).toPlainString();
            throw new InvalidTripMetricsException(
                    "Energy usage per km "
                            + formattedEfficiency
                            + " is outside expected range "
                            + MIN_EFFICIENCY_KWH_PER_KM.toPlainString()
                            + " - "
                            + MAX_EFFICIENCY_KWH_PER_KM.toPlainString()
                            + " kWh/km");
        }
    }
}
