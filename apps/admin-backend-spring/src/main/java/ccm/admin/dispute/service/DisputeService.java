package ccm.admin.dispute.service;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.dispute.dto.request.UpdateDisputeStatusRequest;
import ccm.admin.dispute.dto.response.DisputeDetailResponse;
import ccm.admin.dispute.dto.response.DisputeSummaryResponse;

/**
 * Service interface for Dispute management
 * Handles business logic for dispute operations
 */
public interface DisputeService {

    /**
     * Get all disputes with filtering, sorting, and pagination
     *
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @param sortBy Field to sort by (default: createdAt)
     * @param direction Sort direction (asc/desc)
     * @param keyword Search keyword for description
     * @param status Filter by dispute status
     * @return Paginated list of dispute summaries
     */
    PageResponse<DisputeSummaryResponse> getAllDisputes(
            int page,
            int size,
            String sortBy,
            String direction,
            String keyword,
            String status
    );

    /**
     * Get detailed information about a specific dispute
     *
     * @param id Dispute ID
     * @return Detailed dispute information
     * @throws jakarta.persistence.EntityNotFoundException if dispute not found
     */
    DisputeDetailResponse getDisputeById(Long id);

    /**
     * Update dispute status and admin note
     *
     * @param id Dispute ID
     * @param request Request containing new status and admin note
     * @throws jakarta.persistence.EntityNotFoundException if dispute not found
     */
    void updateStatus(Long id, UpdateDisputeStatusRequest request);
}
