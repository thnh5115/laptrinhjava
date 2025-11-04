package ccm.cva.verification.application.command;

import java.util.UUID;

public record ApproveVerificationRequestCommand(
        UUID verifierId,
        String notes,
        String idempotencyKey,
        String correlationId
) {}
