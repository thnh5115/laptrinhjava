package ccm.cva.verification.application.service;

import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DefaultValidationEngine implements ValidationEngine {

    private static final int EFFICIENCY_SCALE = 4;
    private static final BigDecimal MIN_EFFICIENCY_KWH_PER_KM = new BigDecimal("0.05");
    private static final BigDecimal MAX_EFFICIENCY_KWH_PER_KM = new BigDecimal("0.35");

    private final VerificationRequestRepository repository;

    public DefaultValidationEngine(VerificationRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public ValidationResult validate(CreateVerificationRequestCommand command) {
        List<String> errors = new ArrayList<>();

        if (command.ownerId() == null) {
            errors.add("ownerId is required");
        }
        if (!StringUtils.hasText(command.tripId())) {
            errors.add("tripId is required");
        }
        if (!StringUtils.hasText(command.checksum())) {
            errors.add("checksum is required");
        }
        validatePositive(command.distanceKm(), "distanceKm", errors);
        validatePositive(command.energyKwh(), "energyKwh", errors);

        if (!errors.isEmpty()) {
            return ValidationResult.failed(errors);
        }

        if (repository.existsByChecksum(command.checksum())) {
            errors.add("A verification request with the same checksum already exists");
        }
        if (command.ownerId() != null && StringUtils.hasText(command.tripId())
            && repository.existsByOwnerIdAndTripId(command.ownerId(), command.tripId().trim())) {
            errors.add("This owner has already submitted verification for the provided trip");
        }

        if (command.distanceKm() != null && command.energyKwh() != null
            && command.distanceKm().signum() > 0) {
            BigDecimal efficiency = command.energyKwh()
                .divide(command.distanceKm(), EFFICIENCY_SCALE, RoundingMode.HALF_UP);
            if (efficiency.compareTo(MIN_EFFICIENCY_KWH_PER_KM) < 0
                || efficiency.compareTo(MAX_EFFICIENCY_KWH_PER_KM) > 0) {
                errors.add("Energy usage per km " + efficiency.toPlainString()
                    + " is outside expected range "
                    + MIN_EFFICIENCY_KWH_PER_KM.toPlainString() + " - "
                    + MAX_EFFICIENCY_KWH_PER_KM.toPlainString() + " kWh/km");
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.ok();
        }
        return ValidationResult.failed(errors);
    }

    private void validatePositive(BigDecimal value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " is required");
        } else if (value.signum() <= 0) {
            errors.add(fieldName + " must be greater than zero");
        }
    }
}
