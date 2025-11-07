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
/** web - Interceptor - Request interceptor for web tracking */

public class AuditInterceptor implements HandlerInterceptor {

    private final HttpAuditService httpAuditService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "oldPassword", "newPassword", "confirmPassword",
        "passwordHash", "secret", "token", "apiKey", "privateKey"
    );

    
    public AuditInterceptor(HttpAuditService httpAuditService) {
        this.httpAuditService = httpAuditService;
    }

    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        
        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST") ||
            method.equalsIgnoreCase("PUT") ||
            method.equalsIgnoreCase("DELETE")) {

            
            
            httpAuditService.logFromRequest(request, "Request Started", null, null);
        }

        return true; 
    }

    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST") ||
            method.equalsIgnoreCase("PUT") ||
            method.equalsIgnoreCase("DELETE")) {

            int status = response.getStatus();
            String action = mapAction(method);
            
            
            String body = extractRequestBody(request);
            
            
            String sanitizedBody = sanitizeRequestBody(body, request.getRequestURI());

            httpAuditService.logFromRequest(request, action, sanitizedBody, status);
        }
    }

    

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
    
    
    private String sanitizeRequestBody(String body, String uri) {
        if (body == null || body.isBlank()) {
            return body;
        }
        
        
        if (isSensitiveEndpoint(uri)) {
            return "[REDACTED - Sensitive endpoint: " + uri + "]";
        }
        
        try {
            
            JsonNode jsonNode = objectMapper.readTree(body);
            
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                
                
                for (String field : SENSITIVE_FIELDS) {
                    if (objectNode.has(field)) {
                        objectNode.put(field, "***REDACTED***");
                    }
                }
                
                return objectMapper.writeValueAsString(objectNode);
            }
            
            
            return body;
            
        } catch (Exception e) {
            
            String sanitized = body;
            for (String field : SENSITIVE_FIELDS) {
                
                sanitized = sanitized.replaceAll(
                    "\"" + field + "\"\\s*:\\s*\"[^\"]*\"", 
                    "\"" + field + "\":\"***REDACTED***\""
                );
            }
            return sanitized;
        }
    }
    
    
    private boolean isSensitiveEndpoint(String uri) {
        return uri.contains("/login") || 
               uri.contains("/register") || 
               uri.contains("/password") ||
               uri.contains("/auth/refresh") ||
               uri.contains("/reset-password");
    }
}
