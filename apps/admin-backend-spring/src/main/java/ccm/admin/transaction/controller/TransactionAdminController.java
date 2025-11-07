package ccm.admin.transaction.controller;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.transaction.dto.request.UpdateTransactionStatusRequest;
import ccm.admin.transaction.dto.response.TransactionDetailResponse;
import ccm.admin.transaction.dto.response.TransactionSummaryResponse;
import ccm.admin.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
/** Transaction - REST Controller - Admin endpoints for Transaction management */

public class TransactionAdminController {

    private final TransactionService transactionService;

    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TransactionSummaryResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        
        PageResponse<TransactionSummaryResponse> response = transactionService.getAllTransactions(
            page, size, sort, keyword, status, type
        );
        
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionDetailResponse> getTransactionById(@PathVariable("id") Long id) {
        TransactionDetailResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTransactionStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {
        
        transactionService.updateStatus(id, request);
        return ResponseEntity.ok().build();
    }
}
