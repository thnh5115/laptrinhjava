package ccm.admin.auth.controller;

import ccm.admin.audit.service.HttpAuditService;
import ccm.admin.auth.dto.request.LoginRequest;
import ccm.admin.auth.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller
 * 
 * Endpoints:
 * - POST /api/auth/login - User authentication with JWT token generation
 * - POST /api/auth/refresh - Refresh expired/near-expired JWT tokens
 * 
 * PR-7: FE Integration Blockers - Added refresh token endpoint
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HttpAuditService httpAuditService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService,
                          HttpAuditService httpAuditService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.httpAuditService = httpAuditService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            UserDetails user = userDetailsService.loadUserByUsername(req.getUsername());
            String token = jwtService.generateToken(user);
            
            // AUD-003: Log successful authentication
            httpAuditService.logFromRequest(request, "LOGIN_SUCCESS", null, 200);
            
            // Return token with consistent field name "accessToken"
            return ResponseEntity.ok(Map.of("accessToken", token));
            
        } catch (BadCredentialsException e) {
            // AUD-003 FIX: Log failed authentication attempt
            httpAuditService.logFromRequest(request, "LOGIN_FAILED", 
                    "{\"username\":\"" + req.getUsername() + "\",\"reason\":\"Invalid credentials\"}", 
                    401);
            throw e; // Re-throw to let GlobalExceptionHandler handle the response
        }
    }

    /**
     * Refresh JWT Token
     * PR-7: BLOCKER #3 - Refresh Token Endpoint
     * 
     * Allows users to refresh their JWT tokens without re-authenticating.
     * Accepts expired tokens within a 5-minute grace period.
     * 
     * @param authHeader Authorization header with Bearer token
     * @param request HTTP request for audit logging
     * @return New access token with fresh expiration
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Refresh attempt with missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "status", 401,
                                "error", "Unauthorized",
                                "message", "Missing or invalid Authorization header"
                        ));
            }
            
            String oldToken = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Extract username from expired token (with grace period)
            String username = jwtService.extractUsernameFromExpiredToken(oldToken);
            
            // Load user details and generate new token
            UserDetails user = userDetailsService.loadUserByUsername(username);
            String newToken = jwtService.generateToken(user);
            
            // Log successful refresh
            httpAuditService.logFromRequest(request, "TOKEN_REFRESH_SUCCESS", 
                    "{\"username\":\"" + username + "\"}", 200);
            
            log.info("Token refreshed successfully for user: {}", username);
            
            return ResponseEntity.ok(Map.of("accessToken", newToken));
            
        } catch (IllegalArgumentException e) {
            // Token expired beyond grace period or invalid token
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
            // Unexpected error
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
}
