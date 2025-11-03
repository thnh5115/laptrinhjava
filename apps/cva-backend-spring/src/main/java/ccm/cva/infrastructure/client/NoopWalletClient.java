package ccm.cva.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopWalletClient implements WalletClient {

    @Override
    public void creditOwner(UUID ownerId, BigDecimal credits, String correlationId, String idempotencyKey) {
        log.debug("[WALLET-STUB] Credit owner {} amount={} correlationId={} idempotencyKey={}",
                ownerId, credits, correlationId, idempotencyKey);
    }
}
