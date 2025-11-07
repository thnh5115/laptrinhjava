package ccm.admin.auth.repository;

import ccm.admin.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
/** repository - Service Interface - Manage refresh tokens lifecycle */

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    
    Optional<RefreshToken> findByToken(String token);

    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId " +
           "AND rt.revoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);

    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiredBefore OR rt.revoked = true")
    int deleteExpiredAndRevoked(@Param("expiredBefore") Instant expiredBefore);

    
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.user.id = :userId " +
           "AND rt.revoked = false AND rt.expiresAt > :now")
    boolean hasActiveToken(@Param("userId") Long userId, @Param("now") Instant now);
}
