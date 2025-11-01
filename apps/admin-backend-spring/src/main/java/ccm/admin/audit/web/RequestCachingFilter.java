package ccm.admin.audit.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Filter để wrap request thành ContentCachingRequestWrapper
 * để có thể đọc request body nhiều lần (cho audit logging)
 * 
 * FIXED: Wraps ALL requests. The 500 errors on protected endpoints were caused by
 * LazyInitializationException in UserAdminService, NOT by this filter.
 * Fix applied: Added @Transactional(readOnly = true) to UserAdminService.searchUsers()
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            // Wrap ALL requests to enable request body re-reading for audit logging
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
