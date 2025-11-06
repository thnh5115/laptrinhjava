package ccm.buyer.service;

import ccm.buyer.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TransactionService {
    List<TransactionResponse> getTransactionsByBuyer(Long buyerId);
    TransactionResponse getTransactionById(Long id);
    Page<TransactionResponse> list(Long buyerId, String status, Pageable pageable);
}
