package ccm.admin.audit.web;

import ccm.admin.audit.service.HttpAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditInterceptor
 * Tests PR-4 fix: AUD-001 (sanitize sensitive fields from audit logs)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditInterceptor Tests")
class AuditInterceptorTest {

    @Mock
    private HttpAuditService httpAuditService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ContentCachingRequestWrapper requestWrapper;

    @InjectMocks
    private AuditInterceptor interceptor;

    @BeforeEach
    void setUp() {
        lenient().when(request.getMethod()).thenReturn("POST");
        lenient().when(response.getStatus()).thenReturn(200);
    }

    // ========================================
    // AUD-001: SANITIZE SENSITIVE FIELDS TESTS
    // ========================================

    @Test
    @DisplayName("AUD-001: Should redact password field from JSON request body")
    void testRedactPasswordFromJson() throws Exception {
        // Given: Request with password in JSON body
        String requestBody = "{\"username\":\"admin@carbon.local\",\"password\":\"SecretPass123!\"}";
        when(requestWrapper.getContentAsByteArray()).thenReturn(requestBody.getBytes(StandardCharsets.UTF_8));
        when(requestWrapper.getMethod()).thenReturn("POST");
        when(requestWrapper.getRequestURI()).thenReturn("/api/admin/users");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When: Interceptor processes request
        interceptor.afterCompletion(requestWrapper, response, new Object(), null);

        // Then: Password should be redacted
        verify(httpAuditService, times(1)).logFromRequest(
                eq(requestWrapper),
                eq("CREATE"),
                bodyCaptor.capture(),
                eq(200)
        );

        String capturedBody = bodyCaptor.getValue();
        assertThat(capturedBody).contains("***REDACTED***");
        assertThat(capturedBody).doesNotContain("SecretPass123!");
        assertThat(capturedBody).contains("admin@carbon.local"); // Username should remain
    }

    @Test
    @DisplayName("AUD-001: Should redact multiple sensitive fields")
    void testRedactMultipleSensitiveFields() throws Exception {
        // Given: Request with multiple sensitive fields
        String requestBody = "{\"email\":\"user@test.com\",\"password\":\"pass123\",\"oldPassword\":\"oldpass\",\"newPassword\":\"newpass\"}";
        when(requestWrapper.getContentAsByteArray()).thenReturn(requestBody.getBytes(StandardCharsets.UTF_8));
        when(requestWrapper.getMethod()).thenReturn("POST");
        when(requestWrapper.getRequestURI()).thenReturn("/api/users/change-password");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        interceptor.afterCompletion(requestWrapper, response, new Object(), null);

        // Then: All password fields should be redacted
        verify(httpAuditService).logFromRequest(
                eq(requestWrapper),
                eq("CREATE"),
                bodyCaptor.capture(),
                eq(200)
        );

        String capturedBody = bodyCaptor.getValue();
        assertThat(capturedBody).contains("***REDACTED***");
        assertThat(capturedBody).doesNotContain("pass123");
        assertThat(capturedBody).doesNotContain("oldpass");
        assertThat(capturedBody).doesNotContain("newpass");
    }

    @Test
    @DisplayName("AUD-001: Should redact entire body for sensitive endpoints")
    void testRedactEntireBodyForSensitiveEndpoints() throws Exception {
        // Given: Request to /login endpoint (sensitive)
        String requestBody = "{\"username\":\"admin\",\"password\":\"secret\"}";
        when(requestWrapper.getContentAsByteArray()).thenReturn(requestBody.getBytes(StandardCharsets.UTF_8));
        when(requestWrapper.getMethod()).thenReturn("POST");
        when(requestWrapper.getRequestURI()).thenReturn("/api/auth/login");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        interceptor.afterCompletion(requestWrapper, response, new Object(), null);

        // Then: Entire body should be redacted
        verify(httpAuditService).logFromRequest(
                eq(requestWrapper),
                eq("CREATE"),
                bodyCaptor.capture(),
                eq(200)
        );

        String capturedBody = bodyCaptor.getValue();
        assertThat(capturedBody).contains("[REDACTED - Sensitive endpoint:");
        assertThat(capturedBody).doesNotContain("admin");
        assertThat(capturedBody).doesNotContain("secret");
    }

    @Test
    @DisplayName("AUD-001: Should handle null request body gracefully")
    void testHandleNullRequestBody() throws Exception {
        // Given: Request with null body
        when(requestWrapper.getContentAsByteArray()).thenReturn(new byte[0]);
        when(requestWrapper.getMethod()).thenReturn("POST");
        when(requestWrapper.getRequestURI()).thenReturn("/api/users");

        // When
        interceptor.afterCompletion(requestWrapper, response, new Object(), null);

        // Then: Should not throw exception
        verify(httpAuditService).logFromRequest(
                eq(requestWrapper),
                eq("CREATE"),
                any(),
                eq(200)
        );
    }

    @Test
    @DisplayName("AUD-001: Should preserve non-sensitive fields in JSON")
    void testPreserveNonSensitiveFields() throws Exception {
        // Given: Request with mix of sensitive and non-sensitive fields
        String requestBody = "{\"email\":\"user@test.com\",\"fullName\":\"John Doe\",\"password\":\"secret123\",\"role\":\"ADMIN\"}";
        when(requestWrapper.getContentAsByteArray()).thenReturn(requestBody.getBytes(StandardCharsets.UTF_8));
        when(requestWrapper.getMethod()).thenReturn("POST");
        when(requestWrapper.getRequestURI()).thenReturn("/api/users");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        interceptor.afterCompletion(requestWrapper, response, new Object(), null);

        // Then: Non-sensitive fields should be preserved
        verify(httpAuditService).logFromRequest(
                any(),
                eq("CREATE"),
                bodyCaptor.capture(),
                eq(200)
        );

        String capturedBody = bodyCaptor.getValue();
        assertThat(capturedBody).contains("user@test.com");
        assertThat(capturedBody).contains("John Doe");
        assertThat(capturedBody).contains("ADMIN");
        assertThat(capturedBody).contains("***REDACTED***"); // Password redacted
        assertThat(capturedBody).doesNotContain("secret123");
    }

    @Test
    @DisplayName("Should not log GET requests")
    void testDoNotLogGetRequests() throws Exception {
        // Given: GET request
        when(request.getMethod()).thenReturn("GET");

        // When
        interceptor.afterCompletion(request, response, new Object(), null);

        // Then: Should not log
        verify(httpAuditService, never()).logFromRequest(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should log PUT requests with sanitized body")
    void testLogPutRequests() throws Exception {
        // Given: PUT request
        String requestBody = "{\"id\":1,\"password\":\"newpass\"}";
        when(requestWrapper.getContentAsByteArray()).thenReturn(requestBody.getBytes(StandardCharsets.UTF_8));
        when(requestWrapper.getMethod()).thenReturn("PUT");
        when(requestWrapper.getRequestURI()).thenReturn("/api/users/1");

        // When
        interceptor.afterCompletion(requestWrapper, response, new Object(), null);

        // Then: Should log with redacted password
        verify(httpAuditService, times(1)).logFromRequest(
                eq(requestWrapper),
                eq("UPDATE"),
                argThat(body -> body != null && body.contains("***REDACTED***")),
                eq(200)
        );
    }

    @Test
    @DisplayName("Should log DELETE requests")
    void testLogDeleteRequests() throws Exception {
        // Given: DELETE request
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/users/1");

        // When
        interceptor.afterCompletion(request, response, new Object(), null);

        // Then: Should log
        verify(httpAuditService, times(1)).logFromRequest(
                eq(request),
                eq("DELETE"),
                any(),
                eq(200)
        );
    }
}
