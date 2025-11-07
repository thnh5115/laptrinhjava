package ccm.buyer.dto.request;


import lombok.Data;

@Data
public class CreateTransactionRequest {
  private Long buyerId;
  private Long listingId;
  private Integer qty;
  private Double amount;
}
