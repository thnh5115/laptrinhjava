package ccm.admin.transaction.service;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.transaction.dto.request.UpdateTransactionStatusRequest;
import ccm.admin.transaction.dto.response.TransactionDetailResponse;
import ccm.admin.transaction.dto.response.TransactionSummaryResponse;

/**
 * Service interface for transaction management
 */
public interface TransactionService {
    
    /**
     * Get all transactions with filtering, sorting, and pagination
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param direction Sort direction (asc/desc)
     * @param keyword Search keyword for buyer/seller email
     * @param status Filter by transaction status
     * @param type Filter by transaction type
     * @return Paginated transaction summary list
     */
    PageResponse<TransactionSummaryResponse> getAllTransactions(
        int page, 
        int size, 
        String sortBy, 
        String direction,
        String keyword, 
        String status, 
        String type
    );

    /**
     * Get transaction details by ID
     * 
     * @param id Transaction ID
     * @return Transaction details
     */
    TransactionDetailResponse getTransactionById(Long id);

    /**
     * Update transaction status
     * 
     * @param id Transaction ID
     * @param request Status update request
     */
    void updateStatus(Long id, UpdateTransactionStatusRequest request);
}
