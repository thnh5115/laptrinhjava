package ccm.admin.report.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistoryResponse {
    private Long id;
    private String type;
    private Long generatedBy;
    private String generatedByName;
    private String generatedByEmail;
    private LocalDateTime generatedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dateRange;  // Formatted date range
    private String format;
    private String filePath;
}
