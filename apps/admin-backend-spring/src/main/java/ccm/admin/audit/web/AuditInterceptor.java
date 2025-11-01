package ccm.admin.audit.web;

import ccm.admin.audit.service.HttpAuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final HttpAuditService httpAuditService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // AUD-001: Sensitive field names to redact from audit logs
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "oldPassword", "newPassword", "confirmPassword",
        "passwordHash", "secret", "token", "apiKey", "privateKey"
    );

    // Dùng constructor injection
    public AuditInterceptor(HttpAuditService httpAuditService) {
        this.httpAuditService = httpAuditService;
    }

    /**
     * Ghi lại thông tin trước khi controller xử lý
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Chỉ log các hành động thay đổi dữ liệu
        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST") ||
            method.equalsIgnoreCase("PUT") ||
            method.equalsIgnoreCase("DELETE")) {

            // Don't extract body here - it will be extracted in afterCompletion
            // after the controller has processed it
            httpAuditService.logFromRequest(request, "Request Started", null, null);
        }

        return true; // Cho phép request đi tiếp
    }

    /**
     * Sau khi hoàn thành request (controller + view)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST") ||
            method.equalsIgnoreCase("PUT") ||
            method.equalsIgnoreCase("DELETE")) {

            int status = response.getStatus();
            String action = mapAction(method);
            
            // Extract body from ContentCachingRequestWrapper if available
            String body = extractRequestBody(request);
            
            // AUD-001 FIX: Sanitize sensitive fields from request body
            String sanitizedBody = sanitizeRequestBody(body, request.getRequestURI());

            httpAuditService.logFromRequest(request, action, sanitizedBody, status);
        }
    }

    // ===== Helper methods =====

    private String extractRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    return new String(buf, StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (Exception e) {
            return "[unreadable body]";
        }
    }

    private String mapAction(String method) {
        return switch (method.toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "ACTION";
        };
    }
    
    /**
     * AUD-001 FIX: Sanitize sensitive fields from request body
     * Removes passwords and other sensitive data from audit logs
     * 
     * @param body The original request body
     * @param uri The request URI
     * @return Sanitized request body with sensitive fields redacted
     */
    private String sanitizeRequestBody(String body, String uri) {
        if (body == null || body.isBlank()) {
            return body;
        }
        
        // For sensitive endpoints, redact entire body
        if (isSensitiveEndpoint(uri)) {
            return "[REDACTED - Sensitive endpoint: " + uri + "]";
        }
        
        try {
            // Parse JSON and redact sensitive fields
            JsonNode jsonNode = objectMapper.readTree(body);
            
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                
                // Redact sensitive fields
                for (String field : SENSITIVE_FIELDS) {
                    if (objectNode.has(field)) {
                        objectNode.put(field, "***REDACTED***");
                    }
                }
                
                return objectMapper.writeValueAsString(objectNode);
            }
            
            // If not an object, return as-is
            return body;
            
        } catch (Exception e) {
            // If JSON parsing fails, return sanitized version
            String sanitized = body;
            for (String field : SENSITIVE_FIELDS) {
                // Simple string replacement for non-JSON bodies
                sanitized = sanitized.replaceAll(
                    "\"" + field + "\"\\s*:\\s*\"[^\"]*\"", 
                    "\"" + field + "\":\"***REDACTED***\""
                );
            }
            return sanitized;
        }
    }
    
    /**
     * Check if endpoint is sensitive (auth, password reset, etc.)
     */
    private boolean isSensitiveEndpoint(String uri) {
        return uri.contains("/login") || 
               uri.contains("/register") || 
               uri.contains("/password") ||
               uri.contains("/auth/refresh") ||
               uri.contains("/reset-password");
    }
}

