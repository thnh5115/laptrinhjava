package ccm.buyer.dto.response;

import ccm.buyer.enums.TrStatus;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CreditOrderResponse {
  private Long id;
  private Long buyerId;
  private int quantity;
  private double price;
  private TrStatus status;
}
