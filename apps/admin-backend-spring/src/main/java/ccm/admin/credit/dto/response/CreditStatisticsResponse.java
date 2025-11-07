package ccm.admin.credit.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditStatisticsResponse {
    private Long totalCredits;
    private Long availableCredits;
    private Long listedCredits;
    private Long soldCredits;
    private Long reservedCredits;
    private BigDecimal totalAmountGenerated;
    private BigDecimal totalAmountSold;
    private BigDecimal totalRevenue;
    private Double salesRate;  // (sold / total) * 100
}
