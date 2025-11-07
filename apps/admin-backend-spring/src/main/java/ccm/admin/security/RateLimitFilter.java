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

@Component
/** security - Filter - Request filter for security processing */

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    
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
        
        
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/refresh")) {
            String clientIp = getClientIp(request);
            
            
            Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());
            
            
            if (bucket.tryConsume(1)) {
                
                filterChain.doFilter(request, response);
            } else {
                
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
            
            filterChain.doFilter(request, response);
        }
    }

    
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(CAPACITY)
                        .refillGreedy(CAPACITY, REFILL_DURATION)
                        .build())
                .build();
    }

    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            
            
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
