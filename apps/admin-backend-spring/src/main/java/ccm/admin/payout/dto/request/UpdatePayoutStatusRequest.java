package ccm.admin.payout.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayoutStatusRequest {
    private String notes;  // Admin notes or rejection reason
}
