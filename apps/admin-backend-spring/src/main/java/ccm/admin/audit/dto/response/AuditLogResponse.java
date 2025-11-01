package ccm.admin.audit.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private String username;
    private String method;
    private String endpoint;
    private String action;
    private String ip;
    private Integer status;
    private Instant createdAt;
}
