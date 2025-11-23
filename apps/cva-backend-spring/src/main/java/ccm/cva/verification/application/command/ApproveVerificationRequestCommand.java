package ccm.cva.verification.application.command;



public record ApproveVerificationRequestCommand(
        Long verifierId,
        String notes,
        String idempotencyKey,
        String correlationId
) {}
