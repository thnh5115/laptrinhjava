package ccm.admin.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionSummaryResponse(
    Long id,
    String transactionCode,
    String buyerEmail,
    String sellerEmail,
    BigDecimal totalPrice,
    String status,
    LocalDateTime createdAt
) {}
