package ccm.admin.journey.service.impl;

import ccm.admin.journey.dto.response.JourneyDetailResponse;
import ccm.admin.journey.dto.response.JourneyStatisticsResponse;
import ccm.admin.journey.dto.response.JourneySummaryResponse;
import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.entity.enums.JourneyStatus;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.admin.journey.service.JourneyAdminService;
import ccm.admin.journey.spec.JourneySpecification;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
/** Journey - Service Implementation - Business logic for Journey operations (READ-ONLY) */

public class JourneyAdminServiceImpl implements JourneyAdminService {

    private final JourneyRepository journeyRepository;
    private final UserRepository userRepository;

    /** List journeys with filtering - READ ONLY */
    @Override
    @Transactional(readOnly = true)
    public Page<JourneySummaryResponse> listJourneys(
            String keyword,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Long userId,
            Pageable pageable) {

        log.info("Listing journeys: keyword={}, status={}, fromDate={}, toDate={}, userId={}",
                keyword, status, fromDate, toDate, userId);

        // Build specifications
        Specification<Journey> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(JourneySpecification.hasKeyword(keyword));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and(JourneySpecification.hasStatus(status));
        }
        if (fromDate != null) {
            spec = spec.and(JourneySpecification.hasJourneyDateFrom(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(JourneySpecification.hasJourneyDateTo(toDate));
        }
        if (userId != null) {
            spec = spec.and(JourneySpecification.hasUserId(userId));
        }

        // Fetch and map
        Page<Journey> journeys = journeyRepository.findAll(spec, pageable);

        return journeys.map(this::mapToSummaryResponse);
    }

    /** Get journey detail - READ ONLY */
    @Override
    @Transactional(readOnly = true)
    public JourneyDetailResponse getJourneyDetail(Long id) {
        log.info("Getting journey detail: id={}", id);

        Journey journey = journeyRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Journey not found with id: " + id));

        return mapToDetailResponse(journey);
    }

    /** Get journey statistics - READ ONLY */
    @Override
    @Transactional(readOnly = true)
    public JourneyStatisticsResponse getJourneyStatistics() {
        log.info("Calculating journey statistics");

        long totalJourneys = journeyRepository.getTotalCount();
        long pendingJourneys = journeyRepository.countByStatus(JourneyStatus.PENDING);
        long verifiedJourneys = journeyRepository.countByStatus(JourneyStatus.VERIFIED);
        long rejectedJourneys = journeyRepository.countByStatus(JourneyStatus.REJECTED);

        BigDecimal totalCreditsGenerated = journeyRepository.calculateTotalCreditsGenerated();

        // Calculate verification rate
        double verificationRate = totalJourneys > 0
                ? ((double) verifiedJourneys / totalJourneys) * 100.0
                : 0.0;

        return JourneyStatisticsResponse.builder()
                .totalJourneys(totalJourneys)
                .pendingJourneys(pendingJourneys)
                .verifiedJourneys(verifiedJourneys)
                .rejectedJourneys(rejectedJourneys)
                .totalCreditsGenerated(totalCreditsGenerated)
                .verificationRate(BigDecimal.valueOf(verificationRate)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue())
                .build();
    }

    // ===== PRIVATE MAPPING METHODS =====

    private JourneySummaryResponse mapToSummaryResponse(Journey journey) {
        String userEmail = userRepository.findById(journey.getUserId())
                .map(User::getEmail)
                .orElse("Unknown");

        return JourneySummaryResponse.builder()
                .id(journey.getId())
                .userId(journey.getUserId())
                .userEmail(userEmail)
                .journeyDate(journey.getJourneyDate())
                .distanceKm(journey.getDistanceKm())
                .creditsGenerated(journey.getCreditsGenerated())
                .status(journey.getStatus())
                .createdAt(journey.getCreatedAt())
                .build();
    }

    private JourneyDetailResponse mapToDetailResponse(Journey journey) {
        User owner = userRepository.findById(journey.getUserId())
                .orElse(null);

        String verifiedByEmail = null;
        if (journey.getVerifiedBy() != null) {
            verifiedByEmail = userRepository.findById(journey.getVerifiedBy())
                    .map(User::getEmail)
                    .orElse("Unknown");
        }

        return JourneyDetailResponse.builder()
                .id(journey.getId())
                .userId(journey.getUserId())
                .userEmail(owner != null ? owner.getEmail() : "Unknown")
                .userName(owner != null ? owner.getFullName() : "Unknown")
                .journeyDate(journey.getJourneyDate())
                .startLocation(journey.getStartLocation())
                .endLocation(journey.getEndLocation())
                .distanceKm(journey.getDistanceKm())
                .energyUsedKwh(journey.getEnergyUsedKwh())
                .creditsGenerated(journey.getCreditsGenerated())
                .status(journey.getStatus())
                .verifiedBy(journey.getVerifiedBy())
                .verifiedByEmail(verifiedByEmail)
                .verifiedAt(journey.getVerifiedAt())
                .rejectionReason(journey.getRejectionReason())
                .createdAt(journey.getCreatedAt())
                .build();
    }
}
