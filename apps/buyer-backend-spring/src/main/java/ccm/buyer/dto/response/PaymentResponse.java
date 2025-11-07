package ccm.buyer.dto.response;

import ccm.buyer.enums.PayStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class PaymentResponse {
  private Long id;
  private Long trId;
  private String method;
  private String ref;
  private Double amount;
  private PayStatus status;
  private LocalDateTime createdAt; 
}
