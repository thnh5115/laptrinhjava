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
/** Audit - REST Controller - Admin endpoints for Audit management */

public class AuditAdminController {

    private final AuditLogService auditLogService;

    
    @GetMapping
    public PageResponse<AuditLogResponse> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String username) {

        log.info("GET /api/admin/audit - page: {}, size: {}, sort: {}, keyword: {}, username: {}",
                page, size, sort, keyword, username);

        return auditLogService.getAuditLogs(page, size, sort, keyword, username);
    }

    
    /** GET /api/admin/audit/summary - perform operation */
    @GetMapping("/summary")
    public AuditSummaryResponse getSummary() {
        log.info("GET /api/admin/audit/summary");
        return auditLogService.getSummary();
    }

    
    @GetMapping("/charts")
    public AuditChartResponse getCharts(@RequestParam(defaultValue = "7") int days) {
        log.info("GET /api/admin/audit/charts - days: {}", days);
        return auditLogService.getCharts(days);
    }
}
