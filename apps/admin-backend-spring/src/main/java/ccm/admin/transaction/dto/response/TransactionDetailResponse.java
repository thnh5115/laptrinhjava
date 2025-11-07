package ccm.admin.transaction.dto.response;

import ccm.admin.transaction.entity.Transaction;

import java.time.LocalDateTime;

public record TransactionDetailResponse(
    Long id,
    String transactionCode,
    String buyerEmail,
    String sellerEmail,
    Double amount,
    Double totalPrice,
    String status,
    String type,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public TransactionDetailResponse(Transaction t) {
        this(
            t.getId(),
            t.getTransactionCode(),
            t.getBuyerEmail(),
            t.getSellerEmail(),
            t.getAmount(),
            t.getTotalPrice(),
            t.getStatus().name(),
            t.getType().name(),
            t.getCreatedAt(),
            t.getUpdatedAt()
        );
    }
}
