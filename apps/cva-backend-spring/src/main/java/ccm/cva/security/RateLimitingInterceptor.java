package ccm.cva.security;

import ccm.cva.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties properties;

    public RateLimitingInterceptor(RateLimiterService rateLimiterService, RateLimitProperties properties) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            rateLimited = handlerMethod.getBeanType().getAnnotation(RateLimited.class);
        }
        if (rateLimited == null) {
            return true;
        }

        RateLimitProperties.RateRule rule = properties.resolve(rateLimited.value());
        if (rule == null) {
            return true;
        }

        String key = buildKey(rateLimited.value(), request);
        Duration window = rule.getWindow();
        int limit = Math.max(rule.getLimit(), 1);
        boolean allowed = rateLimiterService.tryConsume(key, limit, window);
        if (!allowed) {
            reject(response, window, limit);
            return false;
        }
        return true;
    }

    private String buildKey(String name, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication != null && authentication.isAuthenticated()
            ? authentication.getName()
            : request.getRemoteAddr();
        return name + ":" + Objects.toString(principal, "anonymous");
    }

    private void reject(HttpServletResponse response, Duration window, int limit) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        long windowSeconds = Math.max(window.getSeconds(), 1L);
        String body = "{\"message\":\"Too many requests\",\"limit\":" + limit
            + ",\"windowSeconds\":" + windowSeconds + "}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}