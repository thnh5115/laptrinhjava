package ccm.admin.audit.service;

import ccm.admin.audit.entity.HttpAuditLog;
import ccm.admin.audit.repository.HttpAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class HttpAuditService {

    private final HttpAuditLogRepository repository;

    public HttpAuditService(HttpAuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Ghi log từ các tham số thô (dùng khi đã có sẵn dữ liệu).
     */
    public void log(String username,
                    String method,
                    String endpoint,
                    String action,
                    String ip,
                    String requestBody,
                    Integer status) {

        HttpAuditLog log = new HttpAuditLog();
        log.setUsername(safeUsername(username));
        log.setMethod(nullToEmpty(method));
        log.setEndpoint(nullToEmpty(endpoint));
        log.setAction(nullToEmpty(action));
        log.setIp(nullToEmpty(ip));
        log.setRequestBody(trimBody(requestBody));
        log.setStatus(status);

        repository.save(log);
    }

    /**
     * Ghi log tiện dụng từ HttpServletRequest (dùng trong Interceptor).
     * - Tự lấy username từ SecurityContext nếu không truyền.
     * - Lấy IP từ X-Forwarded-For hoặc remoteAddr.
     * - Lấy method, endpoint từ request.
     */
    public void logFromRequest(HttpServletRequest request,
                               String action,
                               String requestBody,
                               Integer status) {

        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        String ip = extractClientIp(request);
        String username = resolveUsername();

        log(username, method, endpoint, action, ip, requestBody, status);
    }

    // ===== Helpers =====

    private String resolveUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                return auth.getName();
            }
        } catch (Exception ignored) { }
        return "anonymous";
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Lấy IP đầu tiên trong chuỗi (client thật sự)
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        return request.getRemoteAddr();
    }

    private String trimBody(String body) {
        if (body == null) return null;
        // Tránh log body quá lớn (vd 10KB) – bạn có thể chỉnh tuỳ ý
        int MAX = 10 * 1024;
        if (body.length() > MAX) {
            return body.substring(0, MAX) + "...[truncated]";
        }
        return body;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String safeUsername(String username) {
        if (username == null || username.isBlank()) return resolveUsername();
        return username;
    }
}
