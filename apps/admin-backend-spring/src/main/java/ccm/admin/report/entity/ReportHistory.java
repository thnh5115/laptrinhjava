package ccm.admin.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** ReportHistory - Entity - Track history of generated reports */

public class ReportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of report (e.g., TRANSACTION, USER, LISTING, CARBON_CREDIT) */
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /** Admin user who generated the report */
    @Column(name = "generated_by", nullable = false)
    private Long generatedBy;

    /** When the report was generated */
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    /** Report start date (for date-range reports) */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Report end date (for date-range reports) */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Export format (CSV, EXCEL, PDF) */
    @Column(name = "format", nullable = false, length = 20)
    private String format;

    /** File path or URL where report is stored */
    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    /** Additional parameters used for report generation */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
