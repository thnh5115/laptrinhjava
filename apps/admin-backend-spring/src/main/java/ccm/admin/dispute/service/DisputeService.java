package ccm.admin.dispute.service;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.dispute.dto.request.UpdateDisputeStatusRequest;
import ccm.admin.dispute.dto.response.DisputeDetailResponse;
import ccm.admin.dispute.dto.response.DisputeSummaryResponse;

/** service - Service Interface - service business logic and data operations */

public interface DisputeService {

    
    PageResponse<DisputeSummaryResponse> getAllDisputes(
            int page,
            int size,
            String sort,
            String keyword,
            String status
    );

    
    DisputeDetailResponse getDisputeById(Long id);

    
    void updateStatus(Long id, UpdateDisputeStatusRequest request);
}
