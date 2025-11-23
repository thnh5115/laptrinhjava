package ccm.cva.verification.application.query;

import ccm.cva.verification.domain.VerificationStatus;

import java.time.LocalDateTime;


public record VerificationRequestQuery(
        VerificationStatus status,
        Long ownerId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        String search
) {
    public static VerificationRequestQuery empty() {
        return new VerificationRequestQuery(null, null, null, null, null);
    }
}
