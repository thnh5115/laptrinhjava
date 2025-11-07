package ccm.admin.transaction.service;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.transaction.dto.request.UpdateTransactionStatusRequest;
import ccm.admin.transaction.dto.response.TransactionDetailResponse;
import ccm.admin.transaction.dto.response.TransactionSummaryResponse;

/** service - Service Interface - service business logic and data operations */

public interface TransactionService {
    
    
    PageResponse<TransactionSummaryResponse> getAllTransactions(
        int page, 
        int size, 
        String sort,
        String keyword, 
        String status, 
        String type
    );

    
    TransactionDetailResponse getTransactionById(Long id);

    
    void updateStatus(Long id, UpdateTransactionStatusRequest request);
}
