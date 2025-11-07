package ccm.cva.verification.application.query;

import ccm.cva.verification.domain.VerificationStatus;
import java.time.Instant;
import java.util.UUID;

public record VerificationRequestQuery(
        VerificationStatus status,
        UUID ownerId,
        Instant createdFrom,
        Instant createdTo,
        String search
) {
    public static VerificationRequestQuery empty() {
        return new VerificationRequestQuery(null, null, null, null, null);
    }
}
