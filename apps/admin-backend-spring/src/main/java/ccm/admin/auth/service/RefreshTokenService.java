package ccm.admin.auth.service;

import ccm.admin.auth.entity.RefreshToken;
import ccm.admin.auth.repository.RefreshTokenRepository;
import ccm.admin.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
/** service - Service Implementation - Manage refresh tokens lifecycle */

public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.security.jwt.refresh-expiration-ms:604800000}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        
        log.info("RefreshTokenService initialized - Token expiration: {} ms ({} days)", 
                 refreshTokenExpirationMs, refreshTokenExpirationMs / (24 * 60 * 60 * 1000));
    }

    
    /** Create new record - transactional */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        
        int revokedCount = refreshTokenRepository.revokeAllByUserId(user.getId());
        if (revokedCount > 0) {
            log.debug("Revoked {} existing refresh token(s) for user: {}", revokedCount, user.getEmail());
        }

        
        String tokenString = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {} (expires at: {})", user.getEmail(), expiresAt);
        
        return saved;
    }

    
    /** Validate JWT token - modifies data */
    public Optional<RefreshToken> validateRefreshToken(String tokenString) {
        return refreshTokenRepository.findByToken(tokenString)
                .filter(token -> {
                    if (Boolean.TRUE.equals(token.getRevoked())) {
                        log.warn("Attempted to use revoked refresh token: {}", tokenString.substring(0, 8) + "...");
                        return false;
                    }
                    if (token.isExpired()) {
                        log.warn("Attempted to use expired refresh token: {}", tokenString.substring(0, 8) + "...");
                        return false;
                    }
                    return true;
                });
    }

    
    /** Process business logic - transactional */
    @Transactional
    public boolean revokeToken(String tokenString) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(tokenString);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            if (!Boolean.TRUE.equals(token.getRevoked())) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token for user: {}", token.getUser().getEmail());
                return true;
            }
        }
        return false;
    }

    
    /** Process business logic - transactional */
    @Transactional
    public int revokeAllUserTokens(Long userId) {
        int count = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh token(s) for user ID: {}", count, userId);
        return count;
    }

    
    /** Clean up old records - transactional */
    @Transactional
    public int cleanupExpiredTokens() {
        Instant now = Instant.now();
        int count = refreshTokenRepository.deleteExpiredAndRevoked(now);
        log.info("Cleaned up {} expired/revoked refresh tokens", count);
        return count;
    }

    
    /** Process business logic - modifies data */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }
}
