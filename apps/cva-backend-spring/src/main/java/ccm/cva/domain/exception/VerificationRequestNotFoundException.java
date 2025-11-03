package ccm.cva.domain.exception;

import java.util.UUID;

public class VerificationRequestNotFoundException extends RuntimeException {

    public VerificationRequestNotFoundException(UUID id) {
        super("Verification request not found: " + id);
    }
}
