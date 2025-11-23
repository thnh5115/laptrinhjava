package ccm.cva.verification.application.command;



public record RejectVerificationRequestCommand(
        Long verifierId,
        String reason
) {}
