package ccm.admin.report.service.impl;

import ccm.admin.report.dto.response.ReportHistoryResponse;
import ccm.admin.report.entity.ReportHistory;
import ccm.admin.report.repository.ReportHistoryRepository;
import ccm.admin.report.service.ReportHistoryService;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportHistoryServiceImpl implements ReportHistoryService {

    private final ReportHistoryRepository reportHistoryRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public Page<ReportHistoryResponse> getReportHistory(Pageable pageable) {
        log.debug("Getting report history with pagination: {}", pageable);
        
        Page<ReportHistory> historyPage = reportHistoryRepository.findAllByOrderByGeneratedAtDesc(pageable);
        
        return historyPage.map(this::mapToResponse);
    }

    private ReportHistoryResponse mapToResponse(ReportHistory history) {
        // Get admin info
        var builder = ReportHistoryResponse.builder()
                .id(history.getId())
                .type(history.getType())
                .generatedBy(history.getGeneratedBy())
                .generatedAt(history.getGeneratedAt())
                .startDate(history.getStartDate())
                .endDate(history.getEndDate())
                .format(history.getFormat())
                .filePath(history.getFilePath());
        
        // Lookup admin name and email
        if (history.getGeneratedBy() != null) {
            userRepository.findById(history.getGeneratedBy()).ifPresent(user -> {
                builder.generatedByName(user.getFullName());
                builder.generatedByEmail(user.getEmail());
            });
        }

        // Format date range
        String dateRange;
        if (history.getStartDate() != null && history.getEndDate() != null) {
            dateRange = history.getStartDate().format(DATE_FORMATTER) + 
                       " to " + 
                       history.getEndDate().format(DATE_FORMATTER);
        } else if (history.getStartDate() != null) {
            dateRange = "From " + history.getStartDate().format(DATE_FORMATTER);
        } else if (history.getEndDate() != null) {
            dateRange = "Until " + history.getEndDate().format(DATE_FORMATTER);
        } else {
            dateRange = "All time";
        }

        return builder.dateRange(dateRange).build();
    }
}
