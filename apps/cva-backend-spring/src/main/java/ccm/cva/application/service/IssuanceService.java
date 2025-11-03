package ccm.cva.application.service;

import ccm.cva.domain.model.VerificationRequest;

public interface IssuanceService {

    void prepareIssuance(VerificationRequest request);
}
