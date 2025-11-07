package ccm.buyer.dto.response;

import ccm.buyer.enums.TrStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class TransactionResponse {
  private Long id;
  private Long buyerId;
  private Long listingId;
  private Integer qty;
  private Double amount;
  private TrStatus status;
  private LocalDateTime createdAt;
}
