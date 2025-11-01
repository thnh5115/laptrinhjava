package ccm.admin.audit.entity;

import ccm.admin.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "audit_logs",
       indexes = {
           @Index(name = "idx_audit_created_at", columnList = "created_at"),
           @Index(name = "idx_audit_actor_id",   columnList = "actor_id")
       })
@Hidden
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id",
            foreignKey = @ForeignKey(name = "fk_audit_actor"))
    @JsonIgnoreProperties({"role", "passwordHash", "createdAt", "updatedAt", "status"})
    private User actor;

    @Column(name = "actor_role", length = 30)
    private String actorRole;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(name = "details", columnDefinition = "json")
    private String details;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    // Constructors
    public AuditLog() {}

    public AuditLog(Long id, User actor, String actorRole, String action, 
                    String targetType, String targetId, String details, 
                    String ip, String userAgent, java.time.LocalDateTime createdAt) {
        this.id = id;
        this.actor = actor;
        this.actorRole = actorRole;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.ip = ip;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Long id;
        private User actor;
        private String actorRole;
        private String action;
        private String targetType;
        private String targetId;
        private String details;
        private String ip;
        private String userAgent;
        private java.time.LocalDateTime createdAt;

        public AuditLogBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuditLogBuilder actor(User actor) {
            this.actor = actor;
            return this;
        }

        public AuditLogBuilder actorRole(String actorRole) {
            this.actorRole = actorRole;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }

        public AuditLogBuilder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public AuditLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLogBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public AuditLogBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditLogBuilder createdAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(id, actor, actorRole, action, targetType, targetId, 
                               details, ip, userAgent, createdAt);
        }
    }
}
