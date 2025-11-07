package ccm.buyer.dto.response;

import java.time.LocalDateTime;

public record BuyerResponse(
    Long id,
    String name,
    String email,
    Double balance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
