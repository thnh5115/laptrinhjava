package ccm.admin.auth.controller;

import ccm.admin.audit.service.HttpAuditService;
import ccm.admin.auth.dto.request.LoginRequest;
import ccm.admin.auth.dto.response.AuthResponse;
import ccm.admin.auth.entity.RefreshToken;
import ccm.admin.auth.security.JwtService;
import ccm.admin.auth.service.RefreshTokenService;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
/** Auth - REST Controller - Admin endpoints for Auth management */

public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HttpAuditService httpAuditService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    
    @Value("${app.security.jwt.expiration-ms:900000}")
    private long jwtExpirationMs;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService,
                          HttpAuditService httpAuditService,
                          UserRepository userRepository,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.httpAuditService = httpAuditService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    
    @Operation(
        summary = "User Login",
        description = "Authenticate user with email and password. Returns JWT access token (15min), refresh token (7 days), and user information."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        )
    })
    /** POST /api/auth/login - authenticate user */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );

            
            UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());
            String accessToken = jwtService.generateToken(userDetails);
            
            
            User user = userRepository.findWithRoleByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + req.getEmail()));

            
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            
            long expiresInSeconds = jwtExpirationMs / 1000;

            
            String roleName = user.getRole() != null ? user.getRole().getName() : "UNKNOWN";

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFullName() != null ? user.getFullName() : "",
                roleName,
                user.getStatus().toString()
            );

            
            AuthResponse response = new AuthResponse(
                accessToken,                  
                expiresInSeconds,             
                refreshToken.getToken(),      
                userInfo                      
            );
            
            
            httpAuditService.logFromRequest(request, "LOGIN_SUCCESS", null, 200);
            
            log.info("User logged in successfully: {}", user.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            
            httpAuditService.logFromRequest(request, "LOGIN_FAILED", 
                    "{\"email\":\"" + req.getEmail() + "\",\"reason\":\"Invalid credentials\"}", 
                    401);
            throw e; 
        } catch (UsernameNotFoundException e) {
            
            httpAuditService.logFromRequest(request, "LOGIN_FAILED", 
                    "{\"email\":\"" + req.getEmail() + "\",\"reason\":\"User not found\"}", 
                    401);
            throw new BadCredentialsException("Invalid email or password");
        } catch (Exception e) {
            
            log.error("Unexpected error during login for user: {}", req.getEmail(), e);
            httpAuditService.logFromRequest(request, "LOGIN_ERROR", 
                    "{\"email\":\"" + req.getEmail() + "\",\"error\":\"" + e.getMessage() + "\"}", 
                    500);
            throw e; 
        }
    }

    
    @Operation(
        summary = "Refresh Tokens",
        description = "Validate refresh token and generate new access token + refresh token. Implements token rotation (old refresh token is revoked)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tokens refreshed successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request) {
        try {
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Refresh attempt with missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "status", 401,
                                "error", "Unauthorized",
                                "message", "Missing or invalid Authorization header"
                        ));
            }
            
            String oldRefreshTokenString = authHeader.substring(7); 
            
            
            RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(oldRefreshTokenString)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));
            
            
            Long userId = oldRefreshToken.getUser().getId();
            
            
            refreshTokenService.revokeToken(oldRefreshTokenString);
            
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String newAccessToken = jwtService.generateToken(userDetails);
            
            
            long expiresInSeconds = jwtExpirationMs / 1000;
            
            
            httpAuditService.logFromRequest(request, "TOKEN_REFRESH_SUCCESS", 
                    "{\"email\":\"" + user.getEmail() + "\"}", 200);
            
            log.info("Tokens refreshed successfully for user: {}", user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer",
                "expiresIn", expiresInSeconds,
                "refreshToken", newRefreshToken.getToken()
            ));
            
        } catch (IllegalArgumentException e) {
            
            log.warn("Token refresh failed: {}", e.getMessage());
            httpAuditService.logFromRequest(request, "TOKEN_REFRESH_FAILED", 
                    "{\"reason\":\"" + e.getMessage() + "\"}", 401);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", 401,
                            "error", "Unauthorized",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            httpAuditService.logFromRequest(request, "TOKEN_REFRESH_ERROR", 
                    "{\"error\":\"" + e.getMessage() + "\"}", 500);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "An unexpected error occurred during token refresh"
                    ));
        }
    }

    
    @Operation(
        summary = "Get Current User",
        description = "Get authenticated user information from JWT access token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User information retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated or invalid token"
        )
    })
    /** GET /api/auth/me - perform operation */
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails principal,
            HttpServletRequest request) {
        
        
        if (principal == null) {
            httpAuditService.logFromRequest(request, "ME_FAILED", 
                    "{\"reason\":\"No authentication principal\"}", 401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }
        
        try {
            
            String email = principal.getUsername();
            
            
            User user = userRepository.findWithRoleByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

            
            String roleName = user.getRole() != null ? user.getRole().getName() : "UNKNOWN";

            Map<String, Object> userInfo = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "role", roleName,
                "status", user.getStatus().toString()
            );

            httpAuditService.logFromRequest(request, "ME_SUCCESS", null, 200);
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("Error fetching current user: {}", e.getMessage(), e);
            httpAuditService.logFromRequest(request, "ME_ERROR", 
                    "{\"error\":\"" + e.getMessage() + "\"}", 500);
            
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", 
                            "message", "Failed to fetch user information"));
        }
    }

    
    @Operation(
        summary = "Logout",
        description = "Logout user and revoke all refresh tokens. Client must clear access token from storage."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Logged out successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated"
        )
    })
    /** POST /api/auth/logout - invalidate session */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails principal,
            HttpServletRequest request) {
        try {
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
            }
            
            String email = principal.getUsername();
            
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
            
            
            int revokedCount = refreshTokenService.revokeAllUserTokens(user.getId());
            
            
            httpAuditService.logFromRequest(request, "LOGOUT_SUCCESS", 
                    "{\"email\":\"" + email + "\",\"revokedTokens\":" + revokedCount + "}", 200);
            
            log.info("User logged out: {} (revoked {} refresh tokens)", email, revokedCount);
            
            return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "email", email,
                "revokedTokens", revokedCount
            ));
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            httpAuditService.logFromRequest(request, "LOGOUT_ERROR", 
                    "{\"error\":\"" + e.getMessage() + "\"}", 500);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", 
                            "message", "Logout failed"));
        }
    }
}
