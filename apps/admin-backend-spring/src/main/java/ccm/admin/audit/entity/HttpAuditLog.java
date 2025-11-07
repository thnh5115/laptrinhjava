package ccm.admin.audit.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity(name = "HttpAuditLog")
@Table(name = "http_audit_logs",
       indexes = {
           @Index(name = "idx_http_audit_created_at", columnList = "created_at"),
           @Index(name = "idx_http_audit_username", columnList = "username")
       })
/** entity - Entity - JPA entity for entity table */

public class HttpAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;          

    @Column(nullable = false, length = 10)
    private String method;            

    @Column(nullable = false, length = 255)
    private String endpoint;          

    @Column(nullable = false, length = 50)
    private String action;            

    @Column(length = 45)
    private String ip;                

    @Lob
    @Column(name = "request_body", columnDefinition = "LONGTEXT")
    private String requestBody;       

    @Column
    private Integer status;           

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;        

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
