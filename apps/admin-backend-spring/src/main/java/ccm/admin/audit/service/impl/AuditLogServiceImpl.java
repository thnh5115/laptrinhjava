package ccm.admin.audit.service.impl;

import ccm.admin.audit.dto.response.AuditChartResponse;
import ccm.admin.audit.dto.response.AuditLogResponse;
import ccm.admin.audit.dto.response.AuditSummaryResponse;
import ccm.admin.audit.entity.HttpAuditLog;
import ccm.admin.audit.repository.HttpAuditLogRepository;
import ccm.admin.audit.service.AuditLogService;
import ccm.common.dto.paging.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final HttpAuditLogRepository httpAuditLogRepository;

    @Override
    public PageResponse<AuditLogResponse> getAuditLogs(
            int page, int size, String sortBy, String direction,
            String keyword, String username) {

        log.info("Getting audit logs - page: {}, size: {}, sortBy: {}, direction: {}, keyword: {}, username: {}",
                page, size, sortBy, direction, keyword, username);

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        // Build dynamic specification for filtering
        Specification<HttpAuditLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by keyword (search in endpoint)
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("endpoint")),
                        "%" + keyword.toLowerCase() + "%"
                ));
            }

            // Filter by username
            if (username != null && !username.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("username"), username));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        // Execute query
        Page<HttpAuditLog> logs = httpAuditLogRepository.findAll(spec, pageable);

        // Map to response DTO
        List<AuditLogResponse> content = logs.getContent().stream()
                .map(auditLog -> AuditLogResponse.builder()
                        .id(auditLog.getId())
                        .username(auditLog.getUsername())
                        .method(auditLog.getMethod())
                        .endpoint(auditLog.getEndpoint())
                        .action(auditLog.getAction())
                        .ip(auditLog.getIp())
                        .status(auditLog.getStatus())
                        .createdAt(auditLog.getCreatedAt())
                        .build())
                .toList();

        log.info("Found {} audit logs (total: {})", content.size(), logs.getTotalElements());

        return new PageResponse<>(
                content,
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages(),
                logs.isFirst(),
                logs.isLast(),
                sortBy
        );
    }

    @Override
    public AuditSummaryResponse getSummary() {
        log.info("Getting audit summary");

        // Get all logs for summary calculation
        List<HttpAuditLog> allLogs = httpAuditLogRepository.findAll();

        // Calculate statistics
        long totalLogs = allLogs.size();
        long totalUsers = allLogs.stream()
                .map(HttpAuditLog::getUsername)
                .distinct()
                .count();
        long errorCount = allLogs.stream()
                .filter(auditLog -> auditLog.getStatus() != null && auditLog.getStatus() >= 400)
                .count();

        log.info("Summary - totalLogs: {}, totalUsers: {}, errorCount: {}", totalLogs, totalUsers, errorCount);

        return AuditSummaryResponse.builder()
                .totalLogs(totalLogs)
                .totalUsers(totalUsers)
                .errorCount(errorCount)
                .build();
    }

    @Override
    public AuditChartResponse getCharts(int days) {
        log.info("Getting audit charts for last {} days", days);

        // Calculate time range
        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);

        // Get recent logs
        List<HttpAuditLog> recentLogs = httpAuditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getCreatedAt().isAfter(from))
                .toList();

        log.info("Found {} logs in last {} days", recentLogs.size(), days);

        // Group requests by day
        Map<String, Long> requestsByDay = recentLogs.stream()
                .collect(Collectors.groupingBy(
                        auditLog -> auditLog.getCreatedAt().toString().substring(0, 10), // Extract date (YYYY-MM-DD)
                        Collectors.counting()
                ));

        // Group by endpoint and get top 5
        Map<String, Long> topEndpoints = recentLogs.stream()
                .collect(Collectors.groupingBy(HttpAuditLog::getEndpoint, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        log.info("Generated charts - requestsByDay: {} entries, topEndpoints: {} entries",
                requestsByDay.size(), topEndpoints.size());

        return AuditChartResponse.builder()
                .requestsByDay(requestsByDay)
                .topEndpoints(topEndpoints)
                .build();
    }
}
