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
/** service - Service Implementation - Record and query audit logs */

/** @summary <business action> */

public class AuditService {

    private final AuditLogRepository auditRepo;
    private final UserRepository userRepo;

    public void userAction(String action, Long targetUserId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return; 
        }
        String actorEmail = auth.getName();
        User actor = userRepo.findByEmail(actorEmail).orElse(null);

        var log = AuditLog.builder()
                .actor(actor) 
                .actorRole(actor != null && actor.getRole() != null ? actor.getRole().getName() : null)
                .action(action) 
                .targetType("USER")
                .targetId(targetUserId != null ? String.valueOf(targetUserId) : null)
                .details(null) 
                .ip(null)      
                .userAgent(null)
                .build();

        auditRepo.save(log);
    }
}
