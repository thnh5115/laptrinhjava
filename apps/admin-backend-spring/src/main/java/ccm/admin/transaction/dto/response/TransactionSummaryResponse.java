package ccm.admin.transaction.dto.response;

import java.time.LocalDateTime;

public record TransactionSummaryResponse(
    Long id,
    String transactionCode,
    String buyerEmail,
    String sellerEmail,
    Double totalPrice,
    String status,
    LocalDateTime createdAt
) {}
