package ccm.buyer.service.impl;

import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.enums.TransactionStatus;
import ccm.buyer.repository.TransactionRepository;
import ccm.buyer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionResponse> getTransactionsByBuyer(Long buyerId) {
        return transactionRepository.findByOrder_Buyer_Id(buyerId)
                .stream()
                .map(TransactionResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionResponse::of)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    @Override
    public Page<TransactionResponse> list(Long buyerId, String status, Pageable pageable) {
        if (buyerId != null && status != null && !status.isBlank()) {
            TransactionStatus st = parseTransactionStatus(status);
            return transactionRepository
                    .findByOrder_Buyer_IdAndStatus(buyerId, st, pageable)
                    .map(TransactionResponse::of);
        }
        if (buyerId != null) {
            return transactionRepository
                    .findByOrder_Buyer_Id(buyerId, pageable)
                    .map(TransactionResponse::of);
        }
        if (status != null && !status.isBlank()) {
            TransactionStatus st = parseTransactionStatus(status);
            return transactionRepository
                    .findByStatus(st, pageable)
                    .map(TransactionResponse::of);
        }
        return transactionRepository.findAll(pageable).map(TransactionResponse::of);
    }

    private TransactionStatus parseTransactionStatus(String status) {
        try {
            return TransactionStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid transaction status: " + status);
        }
    }
}
