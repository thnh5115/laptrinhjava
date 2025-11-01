package ccm.admin.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Access Denied Handler - handles 403 Forbidden errors
 * Triggered when:
 * - Authenticated user tries to access resource without proper role
 * - User with ROLE_USER tries to access /api/admin/**
 * - Missing @PreAuthorize permission
 */
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        log.warn("Access denied: User attempted to access {} {} without proper authorization",
                request.getMethod(),
                request.getRequestURI());

        log.warn("User: {}, IP: {}, Exception: {}",
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "unknown",
                request.getRemoteAddr(),
                accessDeniedException.getMessage());

        // Build error response
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", Instant.now().toString());
        errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
        errorDetails.put("error", "Forbidden");
        errorDetails.put("message", "Access denied: You don't have permission to access this resource");
        errorDetails.put("path", request.getRequestURI());

        // Set response properties
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
