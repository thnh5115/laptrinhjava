package ccm.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for authentication endpoints to prevent brute force attacks.
 * 
 * <p><b>PR-6 (SEC-002): Rate Limiting on Authentication</b></p>
 * <ul>
 *   <li>Limits login attempts to 5 requests per minute per IP address</li>
 *   <li>Uses Bucket4j token bucket algorithm with in-memory storage</li>
 *   <li>Returns 429 Too Many Requests when limit exceeded</li>
 *   <li>Automatically cleans up old buckets to prevent memory leaks</li>
 * </ul>
 * 
 * <p><b>Security Benefits:</b></p>
 * <ul>
 *   <li>Prevents brute force password attacks (5 attempts/min = max 300/hour)</li>
 *   <li>Protects against credential stuffing attacks</li>
 *   <li>Reduces load on authentication infrastructure during attacks</li>
 *   <li>No impact on legitimate users (5/min is sufficient for normal use)</li>
 * </ul>
 * 
 * @see <a href="https://owasp.org/www-community/controls/Blocking_Brute_Force_Attacks">OWASP Brute Force Prevention</a>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    // Rate limit: 5 requests per minute per IP
    private static final int CAPACITY = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                     @NonNull HttpServletResponse response, 
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Only apply rate limiting to authentication endpoints
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/refresh")) {
            String clientIp = getClientIp(request);
            
            // Get or create bucket for this IP
            Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());
            
            // Try to consume 1 token
            if (bucket.tryConsume(1)) {
                // Token available - allow request
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded - return 429
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, Object> errorBody = Map.of(
                    "error", "Too Many Requests",
                    "message", "Rate limit exceeded. Maximum " + CAPACITY + " requests per minute allowed.",
                    "status", 429
                );
                
                response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            }
        } else {
            // Not an auth endpoint - pass through without rate limiting
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Create a new rate limit bucket with the configured capacity and refill rate.
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(CAPACITY)
                        .refillGreedy(CAPACITY, REFILL_DURATION)
                        .build())
                .build();
    }

    /**
     * Extract client IP address from request.
     * Handles X-Forwarded-For header for proxied requests.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2...)
            // Take the first one (original client IP)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
