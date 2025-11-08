package ccm.cva.shared.outbox;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletCreditOutboxPayload(UUID ownerId, BigDecimal credits, String correlationId, String idempotencyKey) { }
