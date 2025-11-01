package ccm.admin.audit.service;

import ccm.admin.audit.entity.AuditLog;
import ccm.admin.audit.repository.AuditLogRepository;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepo;
    private final UserRepository userRepo;

    public void userAction(String action, Long targetUserId) {
        // Lấy actor hiện tại từ SecurityContext (email làm username)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return; // không có thông tin người thực hiện
        }
        String actorEmail = auth.getName();
        User actor = userRepo.findByEmail(actorEmail).orElse(null);

        var log = AuditLog.builder()
                .actor(actor) // có thể null nếu không tìm thấy
                .actorRole(actor != null && actor.getRole() != null ? actor.getRole().getName() : null)
                .action(action) // ví dụ: USER_CREATE / USER_SUSPEND / USER_DELETE
                .targetType("USER")
                .targetId(targetUserId != null ? String.valueOf(targetUserId) : null)
                .details(null) // tuỳ sau này muốn ghi JSON gì thêm
                .ip(null)      // có thể bổ sung nếu bạn đã có interceptor gắn IP vào ThreadLocal
                .userAgent(null)
                .build();

        auditRepo.save(log);
    }
}
