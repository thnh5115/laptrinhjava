package ccm.buyer.service.impl;

import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.repository.TransactionRepository;
import ccm.buyer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionResponse> getTransactionsByBuyer(Long buyerId) {
        return transactionRepository.findByOrderBuyerId(buyerId)
                .stream().map(TransactionResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionResponse::of)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}
