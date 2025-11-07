package ccm.admin.journey.service;

import ccm.admin.journey.dto.response.JourneyDetailResponse;
import ccm.admin.journey.dto.response.JourneyStatisticsResponse;
import ccm.admin.journey.dto.response.JourneySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/** service - Service Interface - Journey business logic and data operations */

public interface JourneyAdminService {

    /** List journeys with filtering and pagination */
    Page<JourneySummaryResponse> listJourneys(
        String keyword,
        String status,
        LocalDate fromDate,
        LocalDate toDate,
        Long userId,
        Pageable pageable
    );

    /** Get detailed journey information */
    JourneyDetailResponse getJourneyDetail(Long id);

    /** Get journey statistics for CO2 impact dashboard */
    JourneyStatisticsResponse getJourneyStatistics();
}
