package ccm.cva.application.service.dto;

import java.util.UUID;

public record RejectVerificationRequestCommand(
        UUID verifierId,
        String reason
) {}
