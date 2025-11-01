package ccm.admin.listing.service;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.listing.dto.request.ListingFilterRequest;
import ccm.admin.listing.dto.response.ListingSummaryResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Admin listing management
 */
public interface ListingAdminService {
    
    /**
     * Get all listings with filtering and pagination
     * @param req Filter criteria
     * @param pageable Pagination info
     * @return Page of listing summaries
     */
    PageResponse<ListingSummaryResponse> getAllListings(ListingFilterRequest req, Pageable pageable);
    
    /**
     * Get listing details by ID
     * @param id Listing ID
     * @return Listing summary
     */
    ListingSummaryResponse getListingById(Long id);
    
    /**
     * Update listing status (APPROVED/REJECTED)
     * @param id Listing ID
     * @param status New status
     * @return Updated listing
     */
    ListingSummaryResponse updateListingStatus(Long id, String status);
}
