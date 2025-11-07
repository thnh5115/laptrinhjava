package ccm.admin.listing.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingModerationRequest {
    private String reason;  // Rejection or delisting reason
}
