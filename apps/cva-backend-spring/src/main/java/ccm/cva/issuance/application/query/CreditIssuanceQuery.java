package ccm.cva.issuance.application.query;

import java.time.Instant;
import java.util.UUID;

public record CreditIssuanceQuery(
        UUID ownerId,
        Instant createdFrom,
        Instant createdTo,
        String search
) {
}
