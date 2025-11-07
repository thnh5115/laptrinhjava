package ccm.admin.transaction.dto.request;

import ccm.admin.transaction.entity.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTransactionStatusRequest(
    @NotNull(message = "Status is required")
    TransactionStatus status
) {}
