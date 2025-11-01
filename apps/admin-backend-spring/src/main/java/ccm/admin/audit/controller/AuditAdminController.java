package ccm.admin.audit.controller;

import ccm.admin.audit.dto.response.AuditChartResponse;
import ccm.admin.audit.dto.response.AuditLogResponse;
import ccm.admin.audit.dto.response.AuditSummaryResponse;
import ccm.admin.audit.service.AuditLogService;
import ccm.common.dto.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AuditAdminController {

    private final AuditLogService auditLogService;

    /**
     * Get paginated audit logs with filtering and sorting
     *
     * @param page      Page number (default: 0)
     * @param size      Page size (default: 10)
     * @param sortBy    Sort field (default: createdAt)
     * @param direction Sort direction (default: desc)
     * @param keyword   Keyword to filter by endpoint
     * @param username  Filter by username
     * @return Paginated audit logs
     */
    @GetMapping
    public PageResponse<AuditLogResponse> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String username) {

        log.info("GET /api/admin/audit - page: {}, size: {}, sortBy: {}, direction: {}, keyword: {}, username: {}",
                page, size, sortBy, direction, keyword, username);

        return auditLogService.getAuditLogs(page, size, sortBy, direction, keyword, username);
    }

    /**
     * Get audit summary statistics
     *
     * @return Summary with total logs, total users, and error count
     */
    @GetMapping("/summary")
    public AuditSummaryResponse getSummary() {
        log.info("GET /api/admin/audit/summary");
        return auditLogService.getSummary();
    }

    /**
     * Get audit charts data for dashboard
     *
     * @param days Number of days to analyze (default: 7)
     * @return Charts with requests by day and top endpoints
     */
    @GetMapping("/charts")
    public AuditChartResponse getCharts(@RequestParam(defaultValue = "7") int days) {
        log.info("GET /api/admin/audit/charts - days: {}", days);
        return auditLogService.getCharts(days);
    }
}
