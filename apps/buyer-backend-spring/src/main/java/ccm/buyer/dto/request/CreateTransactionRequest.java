package ccm.buyer.dto.request;


import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateTransactionRequest {
  private Long buyerId;
  private Long listingId;
  private BigDecimal qty;
  private BigDecimal amount;
}
