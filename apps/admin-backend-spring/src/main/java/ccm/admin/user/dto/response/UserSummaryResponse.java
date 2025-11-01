package ccm.admin.user.dto.response;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String email,
        String fullName,
        String role,
        String status,
        LocalDateTime createdAt
) {}
