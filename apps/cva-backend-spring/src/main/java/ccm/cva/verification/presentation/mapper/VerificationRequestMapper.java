package ccm.cva.verification.presentation.mapper;

import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.verification.presentation.dto.CreditIssuanceResponse;
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
            request.getNotes(),
            toIssuanceResponse(request.getCreditIssuance())
        );
    }

    private CreditIssuanceResponse toIssuanceResponse(CreditIssuance issuance) {
        if (issuance == null) {
            return null;
        }
        return new CreditIssuanceResponse(
            issuance.getId(),
            issuance.getCo2ReducedKg(),
            issuance.getCreditsRaw(),
            issuance.getCreditsRounded(),
            issuance.getIdempotencyKey(),
            issuance.getCorrelationId(),
            issuance.getCreatedAt()
        );
    }
}
