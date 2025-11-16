package ccm.admin.transaction.service;

import ccm.admin.transaction.dto.request.UpdateTransactionStatusRequest;
import ccm.admin.transaction.entity.Transaction;
import ccm.admin.transaction.entity.TransactionAuditLog;
import ccm.admin.transaction.entity.enums.TransactionStatus;
import ccm.admin.transaction.repository.TransactionAuditLogRepository;
import ccm.admin.transaction.repository.TransactionRepository;
import ccm.admin.transaction.service.impl.TransactionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionServiceImpl
 * 
 * Tests PR-2 (TX-002, TX-003): State machine validation and optimistic locking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService - State Machine & Optimistic Locking Tests (PR-2)")
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionAuditLogRepository auditLogRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .id(1L)
                .transactionCode("TX-001")
                .buyerEmail("buyer@example.com")
                .sellerEmail("seller@example.com")
                .amount(BigDecimal.valueOf(100.0))
                .totalPrice(BigDecimal.valueOf(5000.0))
                .status(TransactionStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("TX-002: Should allow PENDING → APPROVED transition")
    void testValidTransition_PendingToApproved() {
        // Given: Transaction in PENDING status
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.APPROVED);

        // When: Update status to APPROVED
        transactionService.updateStatus(1L, request);

        // Then: Status should be updated
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
    }

    @Test
    @DisplayName("TX-002: Should allow PENDING → REJECTED transition")
    void testValidTransition_PendingToRejected() {
        // Given: Transaction in PENDING status
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.REJECTED);

        // When: Update status to REJECTED
        transactionService.updateStatus(1L, request);

        // Then: Status should be updated
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.REJECTED);
    }

    @Test
    @DisplayName("TX-002: Should reject APPROVED → PENDING transition (invalid)")
    void testInvalidTransition_ApprovedToPending() {
        // Given: Transaction in APPROVED status (final state)
        transaction.setStatus(TransactionStatus.APPROVED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.PENDING);

        // When & Then: Should throw IllegalStateException
        assertThatThrownBy(() -> transactionService.updateStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from APPROVED to PENDING");

        // Verify: Save should NOT be called
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("TX-002: Should reject APPROVED → REJECTED transition (invalid)")
    void testInvalidTransition_ApprovedToRejected() {
        // Given: Transaction in APPROVED status (final state)
        transaction.setStatus(TransactionStatus.APPROVED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.REJECTED);

        // When & Then: Should throw IllegalStateException
        assertThatThrownBy(() -> transactionService.updateStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from APPROVED to REJECTED");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("TX-002: Should reject REJECTED → PENDING transition (invalid)")
    void testInvalidTransition_RejectedToPending() {
        // Given: Transaction in REJECTED status (final state)
        transaction.setStatus(TransactionStatus.REJECTED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.PENDING);

        // When & Then: Should throw IllegalStateException
        assertThatThrownBy(() -> transactionService.updateStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from REJECTED to PENDING");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("TX-002: Should reject REJECTED → APPROVED transition (invalid)")
    void testInvalidTransition_RejectedToApproved() {
        // Given: Transaction in REJECTED status (final state)
        transaction.setStatus(TransactionStatus.REJECTED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.APPROVED);

        // When & Then: Should throw IllegalStateException
        assertThatThrownBy(() -> transactionService.updateStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from REJECTED to APPROVED");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("TX-003: Should handle optimistic locking exception")
    void testOptimisticLocking_ConcurrentModification() {
        // Given: Transaction exists
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        
        // Simulate optimistic lock exception (concurrent modification)
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new OptimisticLockException("Version mismatch"));

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.APPROVED);

        // When & Then: Should throw IllegalStateException with user-friendly message
        assertThatThrownBy(() -> transactionService.updateStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transaction was modified by another user")
                .hasMessageContaining("Please refresh and try again")
                .hasCauseInstanceOf(OptimisticLockException.class);

        // Verify: Save was attempted
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("TX-003: Should successfully save when no concurrent modification")
    void testOptimisticLocking_Success() {
        // Given: Transaction exists and no concurrent modification
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        
        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .transactionCode("TX-001")
                .status(TransactionStatus.APPROVED)
                .build();
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.APPROVED);

        // When: Update status
        transactionService.updateStatus(1L, request);

        // Then: Save should succeed
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when transaction not found")
    void testUpdateStatus_TransactionNotFound() {
        // Given: Transaction does not exist
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.APPROVED);

        // When & Then: Should throw EntityNotFoundException
        assertThatThrownBy(() -> transactionService.updateStatus(999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 999");

        // Verify: Save should NOT be called
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("TX-002: Should reject same-status transition (PENDING → PENDING)")
    void testInvalidTransition_SameStatus() {
        // Given: Transaction in PENDING status
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest(TransactionStatus.PENDING);

        // When & Then: Should throw IllegalStateException (not in allowed transitions)
        assertThatThrownBy(() -> transactionService.updateStatus(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from PENDING to PENDING");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
