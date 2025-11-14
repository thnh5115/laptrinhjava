package ccm.cva.journey.service;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.entity.enums.CreditStatus;
import ccm.admin.credit.repository.CarbonCreditRepository;
import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class JourneyReviewService {

    private static final BigDecimal ICE_EMISSION_FACTOR_KG_PER_KM = new BigDecimal("0.120");
    private static final BigDecimal GRID_EMISSION_FACTOR_KG_PER_KWH = new BigDecimal("0.045");

    private final JourneyRepository journeyRepository;
    private final CarbonCreditRepository carbonCreditRepository;

    public JourneyReviewService(JourneyRepository journeyRepository, CarbonCreditRepository carbonCreditRepository) {
        this.journeyRepository = journeyRepository;
        this.carbonCreditRepository = carbonCreditRepository;
    }

    @Transactional(readOnly = true)
    public Page<Journey> search(JourneyQuery query, Pageable pageable) {
        return journeyRepository.findAll(JourneySpecifications.from(query), pageable);
    }

    @Transactional(readOnly = true)
    public Journey get(Long id) {
        return journeyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Journey %s not found".formatted(id)));
    }

    public Journey approve(Long id, JourneyApprovalCommand command) {
        Journey journey = get(id);
        ensurePending(journey);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal credits = resolveCredits(journey, command.overrideCredits());
        journey.setStatus(JourneyStatus.VERIFIED);
        journey.setVerifiedBy(command.verifierId());
        journey.setVerifiedAt(now);
        journey.setCreditsGenerated(credits);
        journey.setRejectionReason(null);

        Journey saved = journeyRepository.save(journey);
        upsertCarbonCredit(saved, credits);
        return saved;
    }

    public Journey reject(Long id, JourneyRejectionCommand command) {
        Journey journey = get(id);
        ensurePending(journey);
        LocalDateTime now = LocalDateTime.now();
        if (!StringUtils.hasText(command.reason())) {
            throw new DomainValidationException("Rejection reason required", java.util.List.of("reason must be provided"));
        }

        journey.setStatus(JourneyStatus.REJECTED);
        journey.setVerifiedBy(command.verifierId());
        journey.setVerifiedAt(now);
        journey.setRejectionReason(command.reason().trim());
        journey.setCreditsGenerated(null);

        Journey saved = journeyRepository.save(journey);
        carbonCreditRepository.findByJourneyId(saved.getId()).ifPresent(carbonCreditRepository::delete);
        return saved;
    }

    private void ensurePending(Journey journey) {
        if (journey.getStatus() != JourneyStatus.PENDING) {
            throw new DomainValidationException(
                "Journey already processed",
                java.util.List.of("Only pending journeys can be approved or rejected")
            );
        }
    }

    private BigDecimal resolveCredits(Journey journey, BigDecimal overrideCredits) {
        if (overrideCredits != null) {
            if (overrideCredits.signum() <= 0) {
                throw new DomainValidationException(
                    "Invalid override credits",
                    java.util.List.of("Override credits must be greater than zero")
                );
            }
            return overrideCredits.setScale(2, RoundingMode.HALF_EVEN);
        }
        if (journey.getDistanceKm() == null || journey.getEnergyUsedKwh() == null) {
            throw new DomainValidationException(
                "Missing metrics",
                java.util.List.of("Distance and energy metrics are required to compute credits")
            );
        }
        BigDecimal baseline = journey.getDistanceKm().multiply(ICE_EMISSION_FACTOR_KG_PER_KM);
        BigDecimal evEmission = journey.getEnergyUsedKwh().multiply(GRID_EMISSION_FACTOR_KG_PER_KWH);
        BigDecimal reduced = baseline.subtract(evEmission);
        if (reduced.signum() < 0) {
            reduced = BigDecimal.ZERO;
        }
        return reduced.setScale(2, RoundingMode.HALF_EVEN);
    }

    private void upsertCarbonCredit(Journey journey, BigDecimal credits) {
        carbonCreditRepository.findByJourneyId(journey.getId())
            .ifPresentOrElse(existing -> {
                existing.setOwnerId(journey.getUserId());
                existing.setAmount(credits);
                existing.setStatus(CreditStatus.AVAILABLE);
                existing.setPricePerCredit(null);
                existing.setListedAt(null);
                existing.setSoldAt(null);
                existing.setBuyerId(null);
                carbonCreditRepository.save(existing);
            }, () -> {
                CarbonCredit credit = CarbonCredit.builder()
                    .journeyId(journey.getId())
                    .ownerId(journey.getUserId())
                    .amount(credits)
                    .status(CreditStatus.AVAILABLE)
                    .build();
                carbonCreditRepository.save(credit);
            });
    }
}
