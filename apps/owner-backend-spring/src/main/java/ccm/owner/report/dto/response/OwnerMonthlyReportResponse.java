package ccm.owner.report.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerMonthlyReportResponse {
    private Integer year;
    private Map<String, Long> journeysByMonth;
    private Map<String, BigDecimal> creditsByMonth;
    private Map<String, BigDecimal> earningsByMonth;
}