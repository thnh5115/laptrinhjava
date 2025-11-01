package ccm.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RateLimitFilter (PR-6: SEC-002).
 * 
 * Verifies:
 * - Rate limiting blocks excessive requests (5/min limit)
 * - Non-auth endpoints bypass rate limiting
 * - Rate limiting is per-IP (different IPs have separate limits)
 * - 429 status code returned when limit exceeded
 * - X-Forwarded-For header honored for proxied requests
 */
@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private ObjectMapper objectMapper;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        rateLimitFilter = new RateLimitFilter(objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    @DisplayName("Should allow requests within rate limit")
    void testAllowsRequestsWithinLimit() throws Exception {
        // Given: Login endpoint
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.100");

        // When: Make 5 requests (within limit)
        for (int i = 0; i < 5; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
            
            // Then: All requests should pass
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
    }

    @Test
    @DisplayName("Should block requests exceeding rate limit")
    void testBlocksRequestsExceedingLimit() throws Exception {
        // Given: Login endpoint
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.100");

        // When: Make 6 requests (exceeds 5/min limit)
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then: 6th request should be blocked with 429
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
        assertThat(response.getContentAsString()).contains("Maximum 5 requests per minute");
    }

    @Test
    @DisplayName("Should apply rate limiting to /api/auth/login endpoint")
    void testRateLimitAppliedToLogin() throws Exception {
        // Given: Login endpoint
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.100");

        // When: Exceed rate limit
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then: Should be rate limited
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should apply rate limiting to /api/auth/refresh endpoint")
    void testRateLimitAppliedToRefresh() throws Exception {
        // Given: Refresh endpoint
        request.setRequestURI("/api/auth/refresh");
        request.setRemoteAddr("192.168.1.100");

        // When: Exceed rate limit
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then: Should be rate limited
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should NOT apply rate limiting to non-auth endpoints")
    void testNoRateLimitOnNonAuthEndpoints() throws Exception {
        // Given: Non-auth endpoint (user endpoint)
        request.setRequestURI("/api/users");
        request.setRemoteAddr("192.168.1.100");

        // When: Make 10 requests (would exceed limit if applied)
        for (int i = 0; i < 10; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
            
            // Then: All requests should pass (no rate limiting)
            assertThat(response.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        }
    }

    @Test
    @DisplayName("Should rate limit per IP address (different IPs have separate limits)")
    void testRateLimitPerIpAddress() throws Exception {
        // Given: Login endpoint, two different IPs
        request.setRequestURI("/api/auth/login");

        // When: IP1 makes 5 requests
        request.setRemoteAddr("192.168.1.100");
        for (int i = 0; i < 5; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then: IP1's 6th request should be blocked
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        rateLimitFilter.doFilter(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        // When: IP2 makes a request
        request.setRemoteAddr("192.168.1.200");
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        rateLimitFilter.doFilter(request, response, filterChain);

        // Then: IP2's request should pass (separate rate limit)
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header for proxied requests")
    void testUsesXForwardedForHeader() throws Exception {
        // Given: Login endpoint with X-Forwarded-For header (proxied request)
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("10.0.0.1"); // Proxy IP
        request.addHeader("X-Forwarded-For", "203.0.113.42, 10.0.0.1"); // Client IP, Proxy IP

        // When: Make 6 requests (exceeds limit)
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then: Should be rate limited based on client IP (203.0.113.42)
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should return proper error response when rate limited")
    void testErrorResponseFormat() throws Exception {
        // Given: Login endpoint
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.100");

        // When: Exceed rate limit
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            filterChain = new MockFilterChain();
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Then: Should return JSON error with correct fields
        String responseBody = response.getContentAsString();
        assertThat(responseBody).contains("\"error\"");
        assertThat(responseBody).contains("\"message\"");
        assertThat(responseBody).contains("\"status\"");
        assertThat(responseBody).contains("429");
        assertThat(response.getContentType()).isEqualTo("application/json");
    }
}
