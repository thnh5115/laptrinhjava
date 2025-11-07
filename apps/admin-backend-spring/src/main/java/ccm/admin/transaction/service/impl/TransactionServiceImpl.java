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
import ccm.common.util.SortUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

@Service
@RequiredArgsConstructor
@Slf4j
/** Transaction - Service Implementation - Business logic for Transaction operations */

public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionAuditLogRepository auditLogRepository;

    
    private static final Map<TransactionStatus, Set<TransactionStatus>> VALID_TRANSITIONS = Map.of(
        TransactionStatus.PENDING, Set.of(TransactionStatus.APPROVED, TransactionStatus.REJECTED),
        TransactionStatus.APPROVED, Set.of(), 
        TransactionStatus.REJECTED, Set.of()  
    );

    /** Get all records - transactional */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionSummaryResponse> getAllTransactions(
            int page, 
            int size, 
            String sort,
            String keyword, 
            String status, 
            String type) {
        
        
        Sort sortSpec = SortUtils.parseSort(sort);
        
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        
        Specification<Transaction> spec = TransactionSpecification.filter(keyword, status, type);
        
        
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        
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

    /** Process business logic - transactional */
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        return new TransactionDetailResponse(transaction);
    }

    @Override
    @Transactional
    @CacheEvict(value = {
        "reports:summary", 
        "reports:monthly", 
        "analytics:kpis", 
        "analytics:trends", 
        "analytics:disputes"
    }, allEntries = true)
    /** Update status - modifies data */
    public void updateStatus(Long id, UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        TransactionStatus currentStatus = transaction.getStatus();
        TransactionStatus newStatus = request.status();

        
        Set<TransactionStatus> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(newStatus)) {
            log.warn("Invalid transaction status transition: {} -> {} for transaction ID: {}", 
                     currentStatus, newStatus, id);
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s. Allowed transitions from %s: %s",
                              currentStatus, newStatus, currentStatus, allowedTransitions));
        }

        log.info("Updating transaction ID: {} from {} to {}", id, currentStatus, newStatus);

        
        transaction.setStatus(newStatus);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        try {
            
            
            transactionRepository.save(transaction);
            
            
            
            String currentUser = "admin@carbon.local"; 
            
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
