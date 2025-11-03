package ccm.cva.application.service.impl;

import ccm.cva.application.service.ValidationEngine;
import ccm.cva.application.service.dto.CreateVerificationRequestCommand;
import ccm.cva.domain.exception.DuplicateVerificationRequestException;
import ccm.cva.infrastructure.persistence.jpa.VerificationRequestRepository;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultValidationEngine implements ValidationEngine {

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

        validatePositive("distanceKm", command.distanceKm());
        validatePositive("energyKwh", command.energyKwh());
        Objects.requireNonNull(command.ownerId(), "OwnerId must not be null");
        if (command.tripId() == null || command.tripId().isBlank()) {
            throw new IllegalArgumentException("TripId must not be blank");
        }
    }

    private void validatePositive(String field, BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be a positive number");
        }
    }
}
