package ccm.admin.transaction.dto.request;

import ccm.admin.transaction.entity.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating transaction status
 */
public record UpdateTransactionStatusRequest(
    @NotNull(message = "Status is required")
    TransactionStatus status
) {}
