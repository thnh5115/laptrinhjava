package ccm.admin.auth.controller;

import ccm.admin.audit.service.HttpAuditService;
import ccm.admin.auth.dto.request.LoginRequest;
import ccm.admin.auth.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Tests for AuthController
 * PR-7: BLOCKER #3 - Refresh Token Endpoint Tests
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JwtService.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private HttpAuditService httpAuditService;

    @Test
    @DisplayName("POST /api/auth/login - Success with valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange
        UserDetails userDetails = User.builder()
                .username("admin@carbon.local")
                .password("Admin@123")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("admin@carbon.local"))
                .thenReturn(userDetails);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin@carbon.local\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        verify(httpAuditService, times(1))
                .logFromRequest(any(), eq("LOGIN_SUCCESS"), any(), eq(200));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Success with valid token")
    void testRefresh_Success() throws Exception {
        // Arrange
        UserDetails userDetails = User.builder()
                .username("admin@carbon.local")
                .password("Admin@123")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();

        // Generate a valid token
        String validToken = jwtService.generateToken(userDetails);

        when(userDetailsService.loadUserByUsername("admin@carbon.local"))
                .thenReturn(userDetails);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        verify(httpAuditService, times(1))
                .logFromRequest(any(), eq("TOKEN_REFRESH_SUCCESS"), any(), eq(200));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Failure with invalid Authorization header format")
    void testRefresh_InvalidAuthHeaderFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .header("Authorization", "InvalidFormat token123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Failure with completely invalid token")
    void testRefresh_InvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid token. Please login again."));

        verify(httpAuditService, times(1))
                .logFromRequest(any(), eq("TOKEN_REFRESH_FAILED"), any(), eq(401));
    }
}
