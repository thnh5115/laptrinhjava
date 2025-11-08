package ccm.cva.verification.presentation.dto;

import java.util.List;

public record BulkCreateVerificationRequestResponse(
        List<VerificationRequestResponse> created,
        List<BulkCreateVerificationRequestResponse.BulkCreateError> errors
) {
    public record BulkCreateError(int index, String tripId, List<String> messages) { }
}
