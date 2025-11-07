package ccm.admin.auth.entity;

import ccm.admin.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** entity - Entity - JPA entity for entity table */

public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    
    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
