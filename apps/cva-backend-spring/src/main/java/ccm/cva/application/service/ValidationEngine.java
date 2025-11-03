package ccm.cva.application.service;

import ccm.cva.application.service.dto.CreateVerificationRequestCommand;

public interface ValidationEngine {

    void validateNewRequest(CreateVerificationRequestCommand command);
}
