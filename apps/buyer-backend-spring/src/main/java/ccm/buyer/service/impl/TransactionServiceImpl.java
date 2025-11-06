package ccm.buyer.service.impl;

import ccm.buyer.dto.request.CreateTransactionRequest;
import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.TrStatus;
import ccm.buyer.exception.NotFoundException;
import ccm.buyer.repository.TransactionRepository;
import ccm.buyer.service.TransactionService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;

  @Override
  public List<TransactionResponse> list(Long buyerId) {
    List<Transaction> data = (buyerId == null)
        ? transactionRepository.findAll()
        : transactionRepository.findByBuyerId(buyerId);
    return data.stream().map(this::map).toList();
  }

  @Override
  public TransactionResponse create(CreateTransactionRequest req) {
    Transaction tx = Transaction.builder()
        .buyerId(req.getBuyerId())
        .listingId(req.getListingId())
        .qty(req.getQty())
        .amount(req.getAmount())
        .status(TrStatus.PENDING)
        .build();
    tx = transactionRepository.save(tx);
    return map(tx);
  }

  @Override
  public TransactionResponse updateStatus(Long id, TrStatus status) {
    Transaction tx = transactionRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    tx.setStatus(status);
    tx = transactionRepository.save(tx);
    return map(tx);
  }

  private TransactionResponse map(Transaction t) {
    return TransactionResponse.builder()
        .id(t.getId())
        .buyerId(t.getBuyerId())
        .listingId(t.getListingId())
        .qty(t.getQty())
        .amount(t.getAmount())
        .status(t.getStatus())
        .createdAt(t.getCreatedAt())
        .build();
  }
}
