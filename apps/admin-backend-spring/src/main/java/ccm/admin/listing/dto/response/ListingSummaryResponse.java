package ccm.admin.listing.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
/** response - Response DTO - Response model for response data */

public class ListingSummaryResponse {
    private Long id;
    private String title;
    private String description;
    private String ownerEmail;
    private String ownerFullName;
    private BigDecimal price;
    private BigDecimal quantity;
    private String unit;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Approval tracking fields
    private Long approvedBy;
    private String approvedByEmail;
    private LocalDateTime approvedAt;
    private String rejectReason;
}
