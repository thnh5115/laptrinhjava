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

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
/** Dispute - REST Controller - Admin endpoints for Dispute management */

public class DisputeAdminController {

    private final DisputeService disputeService;

    
    @GetMapping
    public PageResponse<DisputeSummaryResponse> getDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        log.info("GET /api/admin/disputes - page: {}, size: {}, keyword: {}, status: {}", 
                page, size, keyword, status);
        return disputeService.getAllDisputes(page, size, sort, keyword, status);
    }

    
    /** GET /api/admin/disputes/{id} - get by ID */
    @GetMapping("/{id}")
    public DisputeDetailResponse getDispute(@PathVariable Long id) {
        log.info("GET /api/admin/disputes/{} - Fetching dispute details", id);
        return disputeService.getDisputeById(id);
    }

    
    /** PUT /api/admin/disputes/{id}/status - update existing record */
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
