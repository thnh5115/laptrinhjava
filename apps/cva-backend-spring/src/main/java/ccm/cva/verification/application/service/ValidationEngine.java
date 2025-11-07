package ccm.cva.verification.application.service;

import ccm.cva.verification.application.command.CreateVerificationRequestCommand;

public interface ValidationEngine {

    ValidationResult validate(CreateVerificationRequestCommand command);
}
