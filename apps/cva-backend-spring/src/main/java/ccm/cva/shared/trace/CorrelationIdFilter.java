package ccm.cva.shared.trace;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Ensures every inbound HTTP request has a correlation id that is logged and
 * propagated to downstream services. If the client omits the header a new id
 * is generated automatically.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = httpRequest.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        MDC.put("requestId", correlationId);
        CorrelationIdHolder.set(correlationId);
        httpResponse.setHeader(HEADER_NAME, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            CorrelationIdHolder.clear();
            MDC.remove("correlationId");
            MDC.remove("requestId");
        }
    }
}
