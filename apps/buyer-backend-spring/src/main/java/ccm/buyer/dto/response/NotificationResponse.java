package ccm.buyer.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    Long buyerId,
    String message,
    LocalDateTime createdAt,
    LocalDateTime readAt
) {}
