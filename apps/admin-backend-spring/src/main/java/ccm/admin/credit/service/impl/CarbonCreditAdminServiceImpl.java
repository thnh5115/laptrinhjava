package ccm.admin.credit.service.impl;

import ccm.admin.credit.dto.response.CreditDetailResponse;
import ccm.admin.credit.dto.response.CreditStatisticsResponse;
import ccm.admin.credit.dto.response.CreditSummaryResponse;
import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.entity.enums.CreditStatus;
import ccm.admin.credit.repository.CarbonCreditRepository;
import ccm.admin.credit.service.CarbonCreditAdminService;
import ccm.admin.credit.spec.CarbonCreditSpecification;
import ccm.admin.journey.entity.Journey;
import ccm.admin.journey.repository.JourneyRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CarbonCreditAdminServiceImpl implements CarbonCreditAdminService {

    private final CarbonCreditRepository creditRepository;
    private final UserRepository userRepository;
    private final JourneyRepository journeyRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CreditSummaryResponse> listCredits(
            Long ownerId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long journeyId,
            Pageable pageable
    ) {
        log.debug("Listing credits - ownerId: {}, status: {}, minPrice: {}, maxPrice: {}, journeyId: {}",
                ownerId, status, minPrice, maxPrice, journeyId);

        // Build dynamic specification
        Specification<CarbonCredit> spec = Specification.allOf();

        if (ownerId != null) {
            spec = spec.and(CarbonCreditSpecification.hasOwnerId(ownerId));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and(CarbonCreditSpecification.hasStatus(status));
        }

        if (minPrice != null) {
            spec = spec.and(CarbonCreditSpecification.hasPriceGreaterThanOrEqual(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(CarbonCreditSpecification.hasPriceLessThanOrEqual(maxPrice));
        }

        if (journeyId != null) {
            spec = spec.and(CarbonCreditSpecification.hasJourneyId(journeyId));
        }

        Page<CarbonCredit> creditPage = creditRepository.findAll(spec, pageable);
        return creditPage.map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditDetailResponse getCreditDetail(Long id) {
        log.debug("Getting credit detail for ID: {}", id);

        CarbonCredit credit = creditRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Carbon credit not found with id: " + id));

        return mapToDetailResponse(credit);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditStatisticsResponse getCreditStatistics() {
        log.debug("Calculating credit statistics");

        long total = creditRepository.getTotalCount();
        long available = creditRepository.countByStatus(CreditStatus.AVAILABLE);
        long listed = creditRepository.countByStatus(CreditStatus.LISTED);
        long sold = creditRepository.countByStatus(CreditStatus.SOLD);
        long reserved = creditRepository.countByStatus(CreditStatus.RESERVED);

        BigDecimal totalAmount = creditRepository.calculateTotalAmountByStatus(CreditStatus.AVAILABLE)
                .add(creditRepository.calculateTotalAmountByStatus(CreditStatus.LISTED))
                .add(creditRepository.calculateTotalAmountByStatus(CreditStatus.SOLD))
                .add(creditRepository.calculateTotalAmountByStatus(CreditStatus.RESERVED));

        BigDecimal soldAmount = creditRepository.calculateTotalAmountByStatus(CreditStatus.SOLD);
        BigDecimal revenue = creditRepository.calculateTotalRevenue();

        double salesRate = total > 0 ? ((double) sold / total) * 100.0 : 0.0;

        return CreditStatisticsResponse.builder()
                .totalCredits(total)
                .availableCredits(available)
                .listedCredits(listed)
                .soldCredits(sold)
                .reservedCredits(reserved)
                .totalAmountGenerated(totalAmount)
                .totalAmountSold(soldAmount)
                .totalRevenue(revenue)
                .salesRate(salesRate)
                .build();
    }

    /** Map entity to summary response */
    private CreditSummaryResponse mapToSummaryResponse(CarbonCredit credit) {
        String ownerEmail = getUserEmail(credit.getOwnerId());

        BigDecimal totalValue = null;
        if (credit.getPricePerCredit() != null && credit.getAmount() != null) {
            totalValue = credit.getAmount().multiply(credit.getPricePerCredit());
        }

        return CreditSummaryResponse.builder()
                .id(credit.getId())
                .ownerId(credit.getOwnerId())
                .ownerEmail(ownerEmail)
                .journeyId(credit.getJourneyId())
                .amount(credit.getAmount())
                .status(credit.getStatus())
                .pricePerCredit(credit.getPricePerCredit())
                .totalValue(totalValue)
                .createdAt(credit.getCreatedAt())
                .build();
    }

    /** Map entity to detail response */
    private CreditDetailResponse mapToDetailResponse(CarbonCredit credit) {
        String ownerEmail = getUserEmail(credit.getOwnerId());
        String buyerEmail = credit.getBuyerId() != null ? getUserEmail(credit.getBuyerId()) : null;

        BigDecimal totalValue = null;
        if (credit.getPricePerCredit() != null && credit.getAmount() != null) {
            totalValue = credit.getAmount().multiply(credit.getPricePerCredit());
        }

        // Get journey details
        Journey journey = journeyRepository.findById(credit.getJourneyId()).orElse(null);
        String startLocation = journey != null ? journey.getStartLocation() : null;
        String endLocation = journey != null ? journey.getEndLocation() : null;
        BigDecimal distanceKm = journey != null ? journey.getDistanceKm() : null;

        return CreditDetailResponse.builder()
                .id(credit.getId())
                .ownerId(credit.getOwnerId())
                .ownerEmail(ownerEmail)
                .journeyId(credit.getJourneyId())
                .amount(credit.getAmount())
                .status(credit.getStatus())
                .pricePerCredit(credit.getPricePerCredit())
                .totalValue(totalValue)
                .listedAt(credit.getListedAt())
                .soldAt(credit.getSoldAt())
                .buyerId(credit.getBuyerId())
                .buyerEmail(buyerEmail)
                .createdAt(credit.getCreatedAt())
                .journeyStartLocation(startLocation)
                .journeyEndLocation(endLocation)
                .journeyDistanceKm(distanceKm)
                .build();
    }

    /** Get user email by ID */
    private String getUserEmail(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }
}
