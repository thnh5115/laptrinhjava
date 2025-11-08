package ccm.cva.verification.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkCreateVerificationRequestPayload(
        @NotEmpty(message = "items must not be empty")
        @Valid List<BulkCreateVerificationRequestItemPayload> items
) {
}
