package ccm.cva.verification.application.command;

import java.util.UUID;

public record RejectVerificationRequestCommand(
        UUID verifierId,
        String reason
) {}
