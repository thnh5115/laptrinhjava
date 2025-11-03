package ccm.cva.application.service.dto;

import java.util.UUID;

public record ApproveVerificationRequestCommand(
        UUID verifierId,
        String notes,
        String idempotencyKey,
        String correlationId
) {}
