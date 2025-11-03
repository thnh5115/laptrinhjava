package ccm.cva.application.service.impl;

import ccm.cva.application.service.IssuanceService;
import ccm.cva.domain.model.VerificationRequest;
import ccm.cva.infrastructure.client.WalletClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultIssuanceService implements IssuanceService {

    private final WalletClient walletClient;

    public DefaultIssuanceService(WalletClient walletClient) {
        this.walletClient = walletClient;
    }

    @Override
    public void prepareIssuance(VerificationRequest request) {
        // Stub for week 1: the actual issuance pipeline (credit calculation, wallet integration)
        // will be plugged in during week 2. We still log requests to prove the orchestration path.
        log.debug("Issuance preparation requested for verification {}", request.getId());
        if (log.isTraceEnabled()) {
            log.trace("Wallet client {} ready for issuance orchestration", walletClient.getClass().getSimpleName());
        }
    }
}
