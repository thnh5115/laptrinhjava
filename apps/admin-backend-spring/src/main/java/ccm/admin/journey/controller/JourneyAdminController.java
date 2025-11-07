package ccm.admin.journey.controller;

import ccm.admin.journey.dto.response.JourneyDetailResponse;
import ccm.admin.journey.dto.response.JourneyStatisticsResponse;
import ccm.admin.journey.dto.response.JourneySummaryResponse;
import ccm.admin.journey.service.JourneyAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/journeys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
/** Journey - REST Controller - Admin endpoints for Journey oversight (READ-ONLY) */

public class JourneyAdminController {

    private final JourneyAdminService journeyAdminService;

    /** GET /api/admin/journeys - List all journeys with filtering */
    @GetMapping
    public ResponseEntity<Page<JourneySummaryResponse>> listJourneys(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {

        Page<JourneySummaryResponse> journeys = journeyAdminService.listJourneys(
                keyword, status, fromDate, toDate, userId, pageable
        );
        return ResponseEntity.ok(journeys);
    }

    /** GET /api/admin/journeys/{id} - Get journey detail */
    @GetMapping("/{id}")
    public ResponseEntity<JourneyDetailResponse> getJourneyDetail(@PathVariable Long id) {
        JourneyDetailResponse journey = journeyAdminService.getJourneyDetail(id);
        return ResponseEntity.ok(journey);
    }

    /** GET /api/admin/journeys/statistics - Get CO2 impact statistics */
    @GetMapping("/statistics")
    public ResponseEntity<JourneyStatisticsResponse> getJourneyStatistics() {
        JourneyStatisticsResponse statistics = journeyAdminService.getJourneyStatistics();
        return ResponseEntity.ok(statistics);
    }
}
