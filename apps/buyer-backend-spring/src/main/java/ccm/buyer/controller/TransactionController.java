package ccm.buyer.controller;

import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(transactionService.getTransactionsByBuyer(buyerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
