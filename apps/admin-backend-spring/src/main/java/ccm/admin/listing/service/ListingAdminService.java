package ccm.admin.listing.service;

import ccm.admin.listing.dto.request.ListingModerationRequest;
import ccm.common.dto.paging.PageResponse;
import ccm.admin.listing.dto.request.ListingFilterRequest;
import ccm.admin.listing.dto.response.ListingSummaryResponse;
import org.springframework.data.domain.Pageable;

/** service - Service Interface - Admin operations with validation and audit */

public interface ListingAdminService {
    
    
    PageResponse<ListingSummaryResponse> getAllListings(ListingFilterRequest req, Pageable pageable);
    
    
    ListingSummaryResponse getListingById(Long id);
    
    
    ListingSummaryResponse updateListingStatus(Long id, String status);
    
    /**
     * Approve a listing
     * @param id Listing ID
     * @param adminId Admin who is approving
     * @return Updated listing
     */
    ListingSummaryResponse approveListing(Long id, Long adminId);
    
    /**
     * Reject a listing
     * @param id Listing ID
     * @param adminId Admin who is rejecting
     * @param request Rejection reason
     * @return Updated listing
     */
    ListingSummaryResponse rejectListing(Long id, Long adminId, ListingModerationRequest request);
    
    /**
     * Delist (remove from marketplace)
     * @param id Listing ID
     * @param adminId Admin who is delisting
     * @param request Delisting reason
     * @return Updated listing
     */
    ListingSummaryResponse delistListing(Long id, Long adminId, ListingModerationRequest request);
}
