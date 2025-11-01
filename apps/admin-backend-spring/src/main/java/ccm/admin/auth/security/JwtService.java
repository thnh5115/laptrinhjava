package ccm.admin.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * JWT (JSON Web Token) Service for authentication token management
 * 
 * Security Configuration:
 * - Algorithm: HS256 (HMAC-SHA256)
 * - Access Token Expiration: 15 minutes (configurable via app.security.jwt.expiration-ms)
 * - Secret Key: Minimum 32 characters (256 bits for HS256)
 * 
 * Token Claims:
 * - sub: username (subject)
 * - iat: issued at timestamp
 * - exp: expiration timestamp
 * - roles: user authorities/roles array
 * 
 * PR-7: FE Integration Blockers - Refresh Token Support
 * Added extractUsernameFromExpiredToken() for refresh token endpoint
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long jwtExpirationMs;
    
    // Grace period for token refresh (5 minutes)
    private static final long REFRESH_GRACE_PERIOD_MS = TimeUnit.MINUTES.toMillis(5);

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms:900000}") long jwtExpirationMs
    ) {
        // Secret >= 32 bytes cho HS256; dùng UTF-8 bytes để tạo key an toàn
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
        
        log.info("JWT Service initialized - Token expiration: {} ms ({} minutes)", 
                 jwtExpirationMs, jwtExpirationMs / 60000);
    }

    /** Tạo JWT với subject = username và claim bổ sung (ví dụ roles). */
    public String generateToken(UserDetails userDetails) {
        return buildToken(Map.of(
                "roles", userDetails.getAuthorities().stream().map(Object::toString).toArray()
        ), userDetails.getUsername());
    }

    /** Lấy username (subject) từ token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** 
     * Extract username from expired token (for refresh endpoint)
     * PR-7: BLOCKER #3 - Refresh Token Support
     * 
     * Allows extraction of username from expired tokens within a grace period
     * for token refresh functionality. This prevents users from having to
     * re-login every 15 minutes.
     * 
     * @param token The JWT token (may be expired)
     * @return Username from token
     * @throws IllegalArgumentException if token is invalid or expired beyond grace period
     */
    public String extractUsernameFromExpiredToken(String token) {
        try {
            // Try normal parsing first
            return extractUsername(token);
        } catch (ExpiredJwtException e) {
            // Token is expired, check if within grace period
            Date expiration = e.getClaims().getExpiration();
            long timeSinceExpiration = System.currentTimeMillis() - expiration.getTime();
            
            if (timeSinceExpiration <= REFRESH_GRACE_PERIOD_MS) {
                // Within grace period, allow refresh
                String username = e.getClaims().getSubject();
                log.debug("Token expired {} ms ago (within {} ms grace period), allowing refresh for user: {}", 
                         timeSinceExpiration, REFRESH_GRACE_PERIOD_MS, username);
                return username;
            } else {
                // Expired beyond grace period
                log.warn("Token expired {} ms ago (exceeds {} ms grace period), refresh not allowed", 
                        timeSinceExpiration, REFRESH_GRACE_PERIOD_MS);
                throw new IllegalArgumentException("Token expired beyond grace period. Please login again.");
            }
        } catch (Exception e) {
            log.error("Invalid token provided for refresh: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token. Please login again.");
        }
    }

    /** Kiểm tra token hợp lệ cho user (đúng subject & chưa hết hạn). */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }

    // ====== helpers nội bộ ======

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)      // payload bổ sung
                .setSubject(subject)         // ai đăng nhập
                .setIssuedAt(now)            // iat
                .setExpiration(exp)          // exp
                .signWith(key, SignatureAlgorithm.HS256) // ký HS256
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> picker) {
        return picker.apply(parseAllClaims(token));
    }

    private Claims parseAllClaims(String token) {
        // parse + verify chữ ký bằng secret key
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
