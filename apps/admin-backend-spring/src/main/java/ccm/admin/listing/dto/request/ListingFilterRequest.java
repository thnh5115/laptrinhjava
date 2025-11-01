package ccm.admin.listing.dto.request;

import lombok.Data;

/**
 * Request DTO for filtering listings
 * Supports keyword search, status filter, and owner email filter
 */
@Data
public class ListingFilterRequest {
    private String keyword;        // Search in title
    private String status;          // PENDING, APPROVED, REJECTED
    private String ownerEmail;      // Filter by owner email
}
