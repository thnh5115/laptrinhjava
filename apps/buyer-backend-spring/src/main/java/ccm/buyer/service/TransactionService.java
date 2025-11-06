package ccm.buyer.service;

import ccm.buyer.dto.response.TransactionResponse;
import java.util.List;

public interface TransactionService {
    List<TransactionResponse> getTransactionsByBuyer(Long buyerId);
    TransactionResponse getTransactionById(Long id);
}
