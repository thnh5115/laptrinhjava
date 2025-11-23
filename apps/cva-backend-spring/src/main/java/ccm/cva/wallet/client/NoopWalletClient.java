package ccm.cva.wallet.client;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class NoopWalletClient implements WalletClient {

    private static final Logger log = LoggerFactory.getLogger(NoopWalletClient.class);

    @Override
    public void credit(Long ownerId, BigDecimal credits, String correlationId, String idempotencyKey) {
        log.debug("[NOOP] wallet credit ownerId={} credits={} correlationId={} idempotencyKey={}",
            ownerId, credits, correlationId, idempotencyKey);
    }
}
