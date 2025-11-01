package ccm.admin.transaction.service.impl;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.transaction.dto.request.UpdateTransactionStatusRequest;
import ccm.admin.transaction.dto.response.TransactionDetailResponse;
import ccm.admin.transaction.dto.response.TransactionSummaryResponse;
import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.TransactionAuditLog;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.TransactionAuditLogRepository;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.transaction.service.TransactionService;
import ccm.admin.transaction.spec.TransactionSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of TransactionService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionAuditLogRepository auditLogRepository;

    /**
     * PR-2 (TX-002): State transition rules for transaction status
     * Defines which status changes are allowed to prevent invalid state transitions
     */
    private static final Map<TransactionStatus, Set<TransactionStatus>> VALID_TRANSITIONS = Map.of(
        TransactionStatus.PENDING, Set.of(TransactionStatus.APPROVED, TransactionStatus.REJECTED),
        TransactionStatus.APPROVED, Set.of(), // Final state - no transitions allowed
        TransactionStatus.REJECTED, Set.of()  // Final state - no transitions allowed
    );

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionSummaryResponse> getAllTransactions(
            int page, 
            int size, 
            String sortBy, 
            String direction,
            String keyword, 
            String status, 
            String type) {
        
        // Build sort
        Sort sort = direction != null && direction.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build specification with filters
        Specification<Transaction> spec = TransactionSpecification.filter(keyword, status, type);
        
        // Query with specification
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        // Map to DTO
        List<TransactionSummaryResponse> content = transactionPage.getContent()
            .stream()
            .map(t -> new TransactionSummaryResponse(
                t.getId(),
                t.getTransactionCode(),
                t.getBuyerEmail(),
                t.getSellerEmail(),
                t.getTotalPrice(),
                t.getStatus().name(),
                t.getCreatedAt()
            ))
            .toList();

        // Build PageResponse
        return new PageResponse<>(
            content,
            transactionPage.getNumber(),
            transactionPage.getSize(),
            transactionPage.getTotalElements(),
            transactionPage.getTotalPages(),
            transactionPage.isFirst(),
            transactionPage.isLast(),
            pageable.getSort().toString()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        return new TransactionDetailResponse(transaction);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        TransactionStatus currentStatus = transaction.getStatus();
        TransactionStatus newStatus = request.status();

        // PR-2 (TX-002): Validate state transition
        Set<TransactionStatus> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(newStatus)) {
            log.warn("Invalid transaction status transition: {} -> {} for transaction ID: {}", 
                     currentStatus, newStatus, id);
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s. Allowed transitions from %s: %s",
                              currentStatus, newStatus, currentStatus, allowedTransitions));
        }

        log.info("Updating transaction ID: {} from {} to {}", id, currentStatus, newStatus);

        // Update status
        transaction.setStatus(newStatus);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        try {
            // PR-2 (TX-003): Save with optimistic locking
            // @Version field will detect concurrent modifications
            transactionRepository.save(transaction);
            
            // PR-2 (TX-004): Create audit log entry
            // TODO: Get current user from SecurityContext
            String currentUser = "admin@carbon.local"; // Placeholder - will get from auth context
            
            TransactionAuditLog auditLog = TransactionAuditLog.builder()
                    .transactionId(transaction.getId())
                    .transactionCode(transaction.getTransactionCode())
                    .oldStatus(currentStatus)
                    .newStatus(newStatus)
                    .changedBy(currentUser)
                    .changedAt(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            
            log.info("Successfully updated transaction ID: {} to status {} (audit logged)", id, newStatus);
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock exception for transaction ID: {}. Version conflict detected.", id);
            throw new IllegalStateException(
                "Transaction was modified by another user. Please refresh and try again.", e);
        }
    }
}
