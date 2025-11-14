package ccm.admin.transaction.dto.response;

import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDetailResponse(
    Long id,
    String transactionCode,
    String buyerEmail,
    String sellerEmail,
    BigDecimal amount,
    BigDecimal totalPrice,
    String status,
    String type,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public TransactionDetailResponse(Transaction t) {
        this(
            t.getId(),
            resolveTransactionCode(t),
            t.getBuyerEmail(),
            t.getSellerEmail(),
            t.getAmount(),
            t.getTotalPrice(),
            t.getStatus() != null ? t.getStatus().name() : null,
            resolveTransactionType(t),
            t.getCreatedAt(),
            t.getUpdatedAt()
        );
    }

    private static String resolveTransactionCode(Transaction t) {
        if (t.getTransactionCode() != null && !t.getTransactionCode().isBlank()) {
            return t.getTransactionCode();
        }
        return String.format("TX-%06d", t.getId());
    }

    private static String resolveTransactionType(Transaction t) {
        return t.getType() != null ? t.getType().name() : TransactionType.CREDIT_PURCHASE.name();
    }
}
