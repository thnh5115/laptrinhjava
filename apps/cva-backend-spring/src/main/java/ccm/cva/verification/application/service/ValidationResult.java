package ccm.cva.verification.application.service;

import java.util.Collections;
import java.util.List;

public record ValidationResult(boolean valid, List<String> messages) {

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failed(List<String> messages) {
        return new ValidationResult(false, Collections.unmodifiableList(messages));
    }
}
