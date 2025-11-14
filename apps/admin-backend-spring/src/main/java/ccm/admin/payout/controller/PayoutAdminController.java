package ccm.admin.payout.controller;

import ccm.admin.payout.dto.request.UpdatePayoutStatusRequest;
import ccm.admin.payout.dto.response.PayoutDetailResponse;
import ccm.admin.payout.dto.response.PayoutStatisticsResponse;
import ccm.admin.payout.dto.response.PayoutSummaryResponse;
import ccm.admin.payout.service.PayoutAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PayoutAdminController {

    private final PayoutAdminService payoutAdminService;

    /**
     * List payout requests with filtering
     * GET /api/admin/payouts?userId=1&status=PENDING&fromDate=2025-01-01T00:00:00&toDate=2025-12-31T23:59:59&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<PayoutSummaryResponse>> listPayouts(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable
    ) {
        Page<PayoutSummaryResponse> payouts = payoutAdminService.listPayouts(
                userId, status, fromDate, toDate, pageable);
        return ResponseEntity.ok(payouts);
    }

    /**
     * Get payout detail
     * GET /api/admin/payouts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PayoutDetailResponse> getPayoutDetail(@PathVariable Long id) {
        PayoutDetailResponse payout = payoutAdminService.getPayoutDetail(id);
        return ResponseEntity.ok(payout);
    }

    /**
     * Get payout statistics
     * GET /api/admin/payouts/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<PayoutStatisticsResponse> getPayoutStatistics() {
        PayoutStatisticsResponse statistics = payoutAdminService.getPayoutStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Approve a payout request
     * POST /api/admin/payouts/{id}/approve
     * Body: { "notes": "Approved for payment processing" }
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<PayoutDetailResponse> approvePayout(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody(required = false) UpdatePayoutStatusRequest request
    ) {
        Long adminId = resolveAdminId(authentication);
        PayoutDetailResponse payout = payoutAdminService.approvePayout(id, adminId, request);
        return ResponseEntity.ok(payout);
    }

    /**
     * Reject a payout request
     * POST /api/admin/payouts/{id}/reject
     * Body: { "notes": "Insufficient documentation provided" }
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<PayoutDetailResponse> rejectPayout(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody(required = false) UpdatePayoutStatusRequest request
    ) {
        Long adminId = resolveAdminId(authentication);
        PayoutDetailResponse payout = payoutAdminService.rejectPayout(id, adminId, request);
        return ResponseEntity.ok(payout);
    }

    private Long resolveAdminId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return parseUserId(userDetails.getUsername());
        }
        return parseUserId(authentication.getName());
    }

    private Long parseUserId(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
