package ccm.admin.credit.service;

import ccm.admin.credit.dto.response.CreditDetailResponse;
import ccm.admin.credit.dto.response.CreditStatisticsResponse;
import ccm.admin.credit.dto.response.CreditSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CarbonCreditAdminService {

    /**
     * List carbon credits with filtering
     * @param ownerId Filter by owner ID
     * @param status Filter by status (AVAILABLE, LISTED, SOLD, RESERVED)
     * @param minPrice Minimum price per credit
     * @param maxPrice Maximum price per credit
     * @param journeyId Filter by journey ID
     * @param pageable Pagination parameters
     * @return Paginated list of credit summaries
     */
    Page<CreditSummaryResponse> listCredits(
            Long ownerId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long journeyId,
            Pageable pageable
    );

    /**
     * Get detailed information about a carbon credit
     * @param id Credit ID
     * @return Credit details with journey and user information
     */
    CreditDetailResponse getCreditDetail(Long id);

    /**
     * Get carbon credit statistics
     * @return Aggregated statistics about credits
     */
    CreditStatisticsResponse getCreditStatistics();
}
