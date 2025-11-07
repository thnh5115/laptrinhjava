package ccm.cva.domain.exception;

import java.util.UUID;

public class DuplicateTripVerificationRequestException extends RuntimeException {

    public DuplicateTripVerificationRequestException(UUID ownerId, String tripId) {
        super("Verification request already exists for owner " + ownerId + " and trip " + tripId);
    }
}
