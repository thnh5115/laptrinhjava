package ccm.buyer.dto.response;

import java.math.BigDecimal;

import ccm.buyer.enums.ListingStatus;
import ccm.buyer.enums.ListingType;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ListingResponse {
  private Long id;
  private Long sellerId;
  private ListingType type;
  private BigDecimal qty;
  private BigDecimal pricePerUnit;
  private ListingStatus status;
}
