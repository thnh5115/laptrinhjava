package ccm.admin.listing.dto.request;

import lombok.Data;

@Data
/** request - Request DTO - Request payload for request operations */

public class ListingFilterRequest {
    private String keyword;        
    private String status;          
    private String ownerEmail;      
}
