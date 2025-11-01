package ccm.admin.transaction.dto.response;

import java.time.LocalDateTime;

/**
 * Summary response for transaction list view
 */
public record TransactionSummaryResponse(
    Long id,
    String transactionCode,
    String buyerEmail,
    String sellerEmail,
    Double totalPrice,
    String status,
    LocalDateTime createdAt
) {}
