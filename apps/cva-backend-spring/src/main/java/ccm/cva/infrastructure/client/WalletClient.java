package ccm.cva.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletClient {

    void creditOwner(UUID ownerId, BigDecimal credits, String correlationId, String idempotencyKey);
}
