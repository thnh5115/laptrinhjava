package ccm.admin.audit.service.impl;

import ccm.admin.audit.dto.response.AuditChartResponse;
import ccm.admin.audit.dto.response.AuditLogResponse;
import ccm.admin.audit.dto.response.AuditSummaryResponse;
import ccm.admin.audit.entity.HttpAuditLog;
import ccm.admin.audit.repository.HttpAuditLogRepository;
import ccm.admin.audit.service.AuditLogService;
import ccm.common.dto.paging.PageResponse;
import ccm.common.util.SortUtils;
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
/** Audit - Service Implementation - Business logic for Audit operations */

/** @summary <business action> */

public class AuditLogServiceImpl implements AuditLogService {

    private final HttpAuditLogRepository httpAuditLogRepository;

    @Override
    public PageResponse<AuditLogResponse> getAuditLogs(
            int page, int size, String sort,
            String keyword, String username) {

        log.info("Getting audit logs - page: {}, size: {}, sort: {}, keyword: {}, username: {}",
                page, size, sort, keyword, username);

        
        Sort sortSpec = SortUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        
        Specification<HttpAuditLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("endpoint")),
                        "%" + keyword.toLowerCase() + "%"
                ));
            }

            
            if (username != null && !username.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("username"), username));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        
        Page<HttpAuditLog> logs = httpAuditLogRepository.findAll(spec, pageable);

        
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
                sort  
        );
    }

    /** Process business logic - modifies data */
    @Override
    public AuditSummaryResponse getSummary() {
        log.info("Getting audit summary");

        
        List<HttpAuditLog> allLogs = httpAuditLogRepository.findAll();

        
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

    /** Process business logic - modifies data */
    @Override
    public AuditChartResponse getCharts(int days) {
        log.info("Getting audit charts for last {} days", days);

        
        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);

        
        List<HttpAuditLog> recentLogs = httpAuditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getCreatedAt().isAfter(from))
                .toList();

        log.info("Found {} logs in last {} days", recentLogs.size(), days);

        
        Map<String, Long> requestsByDay = recentLogs.stream()
                .collect(Collectors.groupingBy(
                        auditLog -> auditLog.getCreatedAt().toString().substring(0, 10), 
                        Collectors.counting()
                ));

        
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
