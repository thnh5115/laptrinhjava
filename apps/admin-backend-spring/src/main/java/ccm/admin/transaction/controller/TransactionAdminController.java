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

/**
 * REST Controller for Admin Transaction Management
 * Allows admins to view, filter, and manage carbon credit transactions
 */
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
public class TransactionAdminController {

    private final TransactionService transactionService;

    /**
     * GET /api/admin/transactions
     * Get all transactions with filtering, sorting, and pagination
     * 
     * Query params:
     * - page: page number (default 0)
     * - size: page size (default 10)
     * - sortBy: field to sort by (default createdAt)
     * - direction: sort direction (default desc)
     * - keyword: search in buyer/seller email or transaction code
     * - status: filter by status (PENDING, APPROVED, REJECTED)
     * - type: filter by type (CREDIT_PURCHASE, CREDIT_SALE, TRANSFER)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TransactionSummaryResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        
        PageResponse<TransactionSummaryResponse> response = transactionService.getAllTransactions(
            page, size, sortBy, direction, keyword, status, type
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/transactions/{id}
     * Get transaction details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionDetailResponse> getTransactionById(@PathVariable Long id) {
        TransactionDetailResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/admin/transactions/{id}/status
     * Update transaction status (approve/reject)
     * 
     * Request body: {"status": "APPROVED"} or {"status": "REJECTED"}
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTransactionStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {
        
        transactionService.updateStatus(id, request);
        return ResponseEntity.ok().build();
    }
}
