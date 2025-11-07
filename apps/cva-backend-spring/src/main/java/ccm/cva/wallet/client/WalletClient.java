package ccm.cva.wallet.client;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletClient {

    void credit(UUID ownerId, BigDecimal credits, String correlationId, String idempotencyKey);
}
