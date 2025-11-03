package ccm.cva.infrastructure.client;

import ccm.cva.domain.model.VerificationRequest;

public interface AuditLogClient {

    void recordVerificationCreated(VerificationRequest request);

    void recordVerificationDecision(VerificationRequest request, String correlationId);
}
