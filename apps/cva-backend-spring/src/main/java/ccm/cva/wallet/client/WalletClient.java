package ccm.cva.wallet.client;

import java.math.BigDecimal;


public interface WalletClient {

    void credit(Long ownerId, BigDecimal credits, String correlationId, String idempotencyKey);
}
