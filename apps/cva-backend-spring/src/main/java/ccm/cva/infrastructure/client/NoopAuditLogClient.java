package ccm.cva.infrastructure.client;

import ccm.cva.domain.model.VerificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopAuditLogClient implements AuditLogClient {

    @Override
    public void recordVerificationCreated(VerificationRequest request) {
        log.debug("[AUDIT-STUB] Verification created: {}", request.getId());
    }

    @Override
    public void recordVerificationDecision(VerificationRequest request, String correlationId) {
        log.debug("[AUDIT-STUB] Verification decision: {} status={} correlationId={}",
                request.getId(), request.getStatus(), correlationId);
    }
}
