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

@Service
@Slf4j
/** security - Service Implementation - Generate and validate JWT tokens */

public class JwtService {

    private final SecretKey key;
    private final long jwtExpirationMs;
    
    
    private static final long REFRESH_GRACE_PERIOD_MS = TimeUnit.MINUTES.toMillis(5);

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms:900000}") long jwtExpirationMs
    ) {
        
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
        
        log.info("JWT Service initialized - Token expiration: {} ms ({} minutes)", 
                 jwtExpirationMs, jwtExpirationMs / 60000);
    }

    
    /** Generate JWT token - modifies data */
    public String generateToken(UserDetails userDetails) {
        return buildToken(Map.of(
                "roles", userDetails.getAuthorities().stream().map(Object::toString).toArray()
        ), userDetails.getUsername());
    }

    
    /** Process business logic - modifies data */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    
    /** Process business logic - modifies data */
    public String extractUsernameFromExpiredToken(String token) {
        try {
            
            return extractUsername(token);
        } catch (ExpiredJwtException e) {
            
            Date expiration = e.getClaims().getExpiration();
            long timeSinceExpiration = System.currentTimeMillis() - expiration.getTime();
            
            if (timeSinceExpiration <= REFRESH_GRACE_PERIOD_MS) {
                
                String username = e.getClaims().getSubject();
                log.debug("Token expired {} ms ago (within {} ms grace period), allowing refresh for user: {}", 
                         timeSinceExpiration, REFRESH_GRACE_PERIOD_MS, username);
                return username;
            } else {
                
                log.warn("Token expired {} ms ago (exceeds {} ms grace period), refresh not allowed", 
                        timeSinceExpiration, REFRESH_GRACE_PERIOD_MS);
                throw new IllegalArgumentException("Token expired beyond grace period. Please login again.");
            }
        } catch (Exception e) {
            log.error("Invalid token provided for refresh: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token. Please login again.");
        }
    }

    
    /** Process business logic - modifies data */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }

    

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)      
                .setSubject(subject)         
                .setIssuedAt(now)            
                .setExpiration(exp)          
                .signWith(key, SignatureAlgorithm.HS256) 
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> picker) {
        return picker.apply(parseAllClaims(token));
    }

    private Claims parseAllClaims(String token) {
        
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
