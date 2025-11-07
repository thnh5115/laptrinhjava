package ccm.admin.credit.controller;

import ccm.admin.credit.dto.response.CreditDetailResponse;
import ccm.admin.credit.dto.response.CreditStatisticsResponse;
import ccm.admin.credit.dto.response.CreditSummaryResponse;
import ccm.admin.credit.service.CarbonCreditAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/credits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CarbonCreditAdminController {

    private final CarbonCreditAdminService carbonCreditAdminService;

    /**
     * List carbon credits with filtering
     * GET /api/admin/credits?ownerId=1&status=LISTED&minPrice=10&maxPrice=100&journeyId=5&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<CreditSummaryResponse>> listCredits(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long journeyId,
            Pageable pageable
    ) {
        Page<CreditSummaryResponse> credits = carbonCreditAdminService.listCredits(
                ownerId, status, minPrice, maxPrice, journeyId, pageable);
        return ResponseEntity.ok(credits);
    }

    /**
     * Get credit detail
     * GET /api/admin/credits/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditDetailResponse> getCreditDetail(@PathVariable Long id) {
        CreditDetailResponse credit = carbonCreditAdminService.getCreditDetail(id);
        return ResponseEntity.ok(credit);
    }

    /**
     * Get credit statistics
     * GET /api/admin/credits/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<CreditStatisticsResponse> getCreditStatistics() {
        CreditStatisticsResponse statistics = carbonCreditAdminService.getCreditStatistics();
        return ResponseEntity.ok(statistics);
    }
}
