package ccm.buyer.dto.response;

import ccm.buyer.entity.Transaction;
import ccm.buyer.entity.TransactionStatus;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private Long orderId;
    private Double totalAmount;
    private String paymentMethod;
    private String transactionRef;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public static TransactionResponse of(Transaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.id = tx.getId();
        r.orderId = tx.getOrder().getId();
        r.totalAmount = tx.getTotalAmount();
        r.paymentMethod = tx.getPaymentMethod();
        r.transactionRef = tx.getTransactionRef();
        r.status = tx.getStatus();
        r.createdAt = tx.getCreatedAt();
        return r;
    }

    // getters/setters ...
}
