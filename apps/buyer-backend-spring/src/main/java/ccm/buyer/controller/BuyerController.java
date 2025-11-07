package ccm.buyer.controller;

import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.entity.Transaction;
import ccm.buyer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer")
@RequiredArgsConstructor
public class BuyerController {

  private final TransactionRepository transactionRepository;

  @GetMapping("/{buyerId}/transactions")
  public ResponseEntity<List<TransactionResponse>> getBuyerTransactions(@PathVariable Long buyerId) {
    List<Transaction> data = transactionRepository.findByBuyerId(buyerId);
    List<TransactionResponse> result = data.stream()
        .map(tx -> TransactionResponse.builder()
            .id(tx.getId())
            .buyerId(tx.getBuyerId())
            .listingId(tx.getListingId())
            .qty(tx.getQty())
            .amount(tx.getAmount())
            .status(tx.getStatus())
            .createdAt(tx.getCreatedAt())
            .build()
        ).toList();
    return ResponseEntity.ok(result);
  }
}
