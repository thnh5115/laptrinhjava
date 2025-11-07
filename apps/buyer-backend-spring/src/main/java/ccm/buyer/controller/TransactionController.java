package ccm.buyer.controller;

import ccm.buyer.dto.request.CreateTransactionRequest;
import ccm.buyer.dto.request.UpdateTransactionStatusRequest;
import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.enums.TrStatus;
import ccm.buyer.service.TransactionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @GetMapping
  public ResponseEntity<List<TransactionResponse>> list(@RequestParam(required = false) Long buyerId) {
    return ResponseEntity.ok(transactionService.list(buyerId));
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> create(@RequestBody CreateTransactionRequest req) {
    return ResponseEntity.ok(transactionService.create(req));
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<TransactionResponse> updateStatus(
      @PathVariable Long id,
      @RequestBody UpdateTransactionStatusRequest req
  ) {
    TrStatus status = (req.getStatus() == null) ? TrStatus.PENDING : req.getStatus();
    return ResponseEntity.ok(transactionService.updateStatus(id, status));
  }
}
