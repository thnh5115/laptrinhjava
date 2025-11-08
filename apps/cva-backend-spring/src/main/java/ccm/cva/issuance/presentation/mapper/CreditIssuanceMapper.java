package ccm.cva.issuance.presentation.mapper;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.presentation.dto.CreditIssuanceHistoryResponse;
import ccm.cva.verification.domain.VerificationRequest;
import org.springframework.stereotype.Component;

@Component
public class CreditIssuanceMapper {

    public CreditIssuanceHistoryResponse toHistoryResponse(CreditIssuance issuance) {
        VerificationRequest request = issuance.getVerificationRequest();
        return new CreditIssuanceHistoryResponse(
            issuance.getId(),
            request != null ? request.getId() : null,
            issuance.getOwnerId(),
            request != null ? request.getTripId() : null,
            request != null ? request.getDistanceKm() : null,
            request != null ? request.getEnergyKwh() : null,
            request != null ? request.getChecksum() : null,
            issuance.getCo2ReducedKg(),
            issuance.getCreditsRaw(),
            issuance.getCreditsRounded(),
            issuance.getIdempotencyKey(),
            issuance.getCorrelationId(),
            issuance.getCreatedAt(),
            request != null ? request.getVerifiedAt() : null,
            request != null ? request.getVerifierId() : null,
            request != null ? request.getNotes() : null
        );
    }
}
