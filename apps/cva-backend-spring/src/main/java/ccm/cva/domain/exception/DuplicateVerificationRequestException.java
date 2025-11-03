package ccm.cva.domain.exception;

public class DuplicateVerificationRequestException extends RuntimeException {

    public DuplicateVerificationRequestException(String checksum) {
        super("Verification request already exists for checksum: " + checksum);
    }
}
