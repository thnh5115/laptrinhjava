package ccm.cva.domain.exception;



public class DuplicateTripVerificationRequestException extends RuntimeException {

    public DuplicateTripVerificationRequestException(Long ownerId, String tripId) {
        super("Verification request already exists for owner " + ownerId + " and trip " + tripId);
    }
}
