package ccm.buyer.dto.response;

import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.TransactionStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class TransactionResponse {
    Long id;
    Long orderId;
    Double amount;
    String transactionRef;
    TransactionStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static TransactionResponse of(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .orderId(tx.getOrder() != null ? tx.getOrder().getId() : null)
                .amount(tx.getAmount())
                .transactionRef(tx.getTransactionRef())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
