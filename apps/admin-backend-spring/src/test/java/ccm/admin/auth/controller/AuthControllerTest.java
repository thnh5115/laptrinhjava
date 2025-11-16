package ccm.admin.auth.controller;

import ccm.admin.audit.service.HttpAuditService;
import ccm.admin.auth.dto.request.LoginRequest;
import ccm.admin.auth.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import ccm.admin.auth.security.JwtService;
import ccm.admin.auth.service.RefreshTokenService;
import ccm.admin.user.entity.User;
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

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

   
    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private HttpAuditService httpAuditService;
    
    @MockBean
    private ccm.admin.user.repository.UserRepository userRepository;

    @MockBean
private ccm.admin.auth.service.RefreshTokenService refreshTokenService;
    @Test
    @DisplayName("POST /api/auth/login - Success with valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin@carbon.local")
                .password("Admin@1234")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
Authentication authentication = new UsernamePasswordAuthenticationToken(
    userDetails, 
    null, 
    userDetails.getAuthorities()
);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("admin@carbon.local"))
                .thenReturn(userDetails);
ccm.admin.user.entity.User mockUser = new ccm.admin.user.entity.User();
mockUser.setId(1L); // Cần thiết cho RefreshToken
mockUser.setEmail("admin@carbon.local");
mockUser.setFullName("Admin User");
// Giả lập Role nếu cần, nếu không thì để null
// mockUser.setRole(new ccm.admin.user.entity.Role("ROLE_ADMIN")); 
// Giả lập Status nếu cần
// mockUser.setStatus(ccm.admin.user.entity.UserStatus.ACTIVE);

when(userRepository.findWithRoleByEmail("admin@carbon.local"))
        .thenReturn(Optional.of(mockUser));

// 2. Dạy cho JwtService trả về một token (VÌ BÂY GIỜ NÓ LÀ MOCK)
when(jwtService.generateToken(any(UserDetails.class)))
        .thenReturn("mocked.access.token.string");

// 3. Dạy cho RefreshTokenService trả về một token
RefreshToken mockRefreshToken = new RefreshToken();
mockRefreshToken.setToken("mocked.refresh.token.string");
when(refreshTokenService.createRefreshToken(any(ccm.admin.user.entity.User.class)))
        .thenReturn(mockRefreshToken);
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@carbon.local\",\"password\":\"Admin@1234\"}"))
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
        
        // 1. Tạo User giả
        User mockUser = User.builder()
                .id(1L)
                .email("admin@carbon.local")
                .fullName("Admin User")
                .build();

        // 2. Tạo Refresh Token (UUID) giả
        String fakeRefreshTokenString = "fake-refresh-token-uuid-12345";
        
        RefreshToken validMockToken = RefreshToken.builder()
                .token(fakeRefreshTokenString)
                .user(mockUser) // Liên kết với user
                .expiresAt(Instant.now().plusSeconds(600)) // Còn hạn
                .revoked(false) // Chưa bị thu hồi
                .build();
        
        // 3. Tạo UserDetails (dùng để tạo Access Token MỚI)
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin@carbon.local")
                .password("password") // không quan trọng
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
        
        // 4. Dạy mock: Khi service validate token giả, trả về token thật
        when(refreshTokenService.validateRefreshToken(eq(fakeRefreshTokenString)))
                .thenReturn(Optional.of(validMockToken));
        
        // 5. Dạy mock: Khi service tìm user bằng ID, trả về user giả
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
        
        // 6. Dạy mock: Khi service tạo Refresh Token MỚI, trả về token mới
        RefreshToken newMockRefreshToken = RefreshToken.builder().token("new-refreshed-token-67890").build();
        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(newMockRefreshToken);
                
        // 7. Dạy mock: Khi service load user (để tạo Access Token), trả về userDetails
        when(userDetailsService.loadUserByUsername(eq("admin@carbon.local")))
                .thenReturn(userDetails);
        
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("new.mocked.access.token.string");

        // 9. Dạy mock: revokeToken phải trả về true (vì nó trả về boolean)
        when(refreshTokenService.revokeToken(eq(fakeRefreshTokenString)))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        // Gửi REFRESH TOKEN (UUID) giả, không phải JWT
                        .header("Authorization", "Bearer " + fakeRefreshTokenString)) 
                .andExpect(status().isOk()) // Mong đợi 200 OK
                .andExpect(jsonPath("$.accessToken").exists()) // Mong đợi có access token mới
                .andExpect(jsonPath("$.refreshToken").value("new-refreshed-token-67890")); // Mong đợi có refresh token mới

        // Verify
        verify(refreshTokenService, times(1)).validateRefreshToken(eq(fakeRefreshTokenString));
        verify(refreshTokenService, times(1)).revokeToken(eq(fakeRefreshTokenString)); // Đảm bảo token cũ bị thu hồi
        verify(refreshTokenService, times(1)).createRefreshToken(eq(mockUser)); // Đảm bảo token mới được tạo
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
        when(refreshTokenService.validateRefreshToken(anyString()))
                .thenReturn(Optional.empty());
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));

        verify(httpAuditService, times(1))
                .logFromRequest(any(), eq("TOKEN_REFRESH_FAILED"), any(), eq(401));
    }
}
