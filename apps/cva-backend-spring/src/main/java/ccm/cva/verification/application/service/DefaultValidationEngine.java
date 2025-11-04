package ccm.cva.verification.application.service;

import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DefaultValidationEngine implements ValidationEngine {

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
