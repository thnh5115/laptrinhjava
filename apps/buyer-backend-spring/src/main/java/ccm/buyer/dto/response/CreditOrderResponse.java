package ccm.buyer.dto.response;

import java.math.BigDecimal;

import ccm.buyer.enums.TrStatus;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CreditOrderResponse {
  private Long id;
  private Long buyerId;
  private BigDecimal qty;
  private BigDecimal price;
  private TrStatus status;
}
