package ccm.buyer.dto.request;

import ccm.buyer.enums.TrStatus;
import lombok.Data;

@Data
public class UpdateTransactionStatusRequest {
  private TrStatus status;
  private String note;
}
