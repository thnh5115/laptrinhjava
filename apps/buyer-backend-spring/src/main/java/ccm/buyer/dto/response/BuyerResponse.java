package ccm.buyer.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BuyerResponse(
    Long id,
    String name,
    String email,
    BigDecimal balance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
