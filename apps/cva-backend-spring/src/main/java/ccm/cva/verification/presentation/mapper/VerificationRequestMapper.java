package ccm.cva.verification.presentation.mapper;

import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.presentation.dto.VerificationRequestResponse;
import org.springframework.stereotype.Component;

@Component
public class VerificationRequestMapper {

    public VerificationRequestResponse toResponse(VerificationRequest request) {
        return new VerificationRequestResponse(
            request.getId(),
            request.getOwnerId(),
            request.getTripId(),
            request.getDistanceKm(),
            request.getEnergyKwh(),
            request.getChecksum(),
            request.getStatus(),
            request.getCreatedAt(),
            request.getVerifiedAt(),
            request.getVerifierId(),
            request.getNotes()
        );
    }
}
