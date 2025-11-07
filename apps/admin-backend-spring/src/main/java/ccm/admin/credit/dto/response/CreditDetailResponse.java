package ccm.admin.credit.dto.response;

import ccm.admin.credit.entity.enums.CreditStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailResponse {
    private Long id;
    private Long ownerId;
    private String ownerEmail;
    private Long journeyId;
    private BigDecimal amount;
    private CreditStatus status;
    private BigDecimal pricePerCredit;
    private BigDecimal totalValue;  // amount * pricePerCredit
    private LocalDateTime listedAt;
    private LocalDateTime soldAt;
    private Long buyerId;
    private String buyerEmail;
    private LocalDateTime createdAt;
    
    // Journey details
    private String journeyStartLocation;
    private String journeyEndLocation;
    private BigDecimal journeyDistanceKm;
}
