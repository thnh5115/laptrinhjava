package ccm.admin.dispute.controller;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.dispute.dto.request.UpdateDisputeStatusRequest;
import ccm.admin.dispute.dto.response.DisputeDetailResponse;
import ccm.admin.dispute.dto.response.DisputeSummaryResponse;
import ccm.admin.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Dispute Management (Admin Only)
 * Provides endpoints for viewing and managing disputes
 */
@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DisputeAdminController {

    private final DisputeService disputeService;

    /**
     * Get all disputes with filtering, sorting, and pagination
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Field to sort by (default: createdAt)
     * @param direction Sort direction (default: desc)
     * @param keyword Search keyword for description
     * @param status Filter by dispute status
     * @return Paginated list of disputes
     */
    @GetMapping
    public PageResponse<DisputeSummaryResponse> getDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        log.info("GET /api/admin/disputes - page: {}, size: {}, keyword: {}, status: {}", 
                page, size, keyword, status);
        return disputeService.getAllDisputes(page, size, sortBy, direction, keyword, status);
    }

    /**
     * Get detailed information about a specific dispute
     *
     * @param id Dispute ID
     * @return Detailed dispute information
     */
    @GetMapping("/{id}")
    public DisputeDetailResponse getDispute(@PathVariable Long id) {
        log.info("GET /api/admin/disputes/{} - Fetching dispute details", id);
        return disputeService.getDisputeById(id);
    }

    /**
     * Update dispute status and admin note
     *
     * @param id Dispute ID
     * @param request Request containing new status and admin note
     */
    @PutMapping("/{id}/status")
    public void updateDispute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDisputeStatusRequest request
    ) {
        log.info("PUT /api/admin/disputes/{}/status - Updating to status: {}", 
                id, request.getStatus());
        disputeService.updateStatus(id, request);
    }
}
