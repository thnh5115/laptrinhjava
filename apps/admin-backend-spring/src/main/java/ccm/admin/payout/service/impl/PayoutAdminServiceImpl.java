package ccm.admin.payout.service.impl;

import ccm.admin.payout.dto.request.UpdatePayoutStatusRequest;
import ccm.admin.payout.dto.response.PayoutDetailResponse;
import ccm.admin.payout.dto.response.PayoutStatisticsResponse;
import ccm.admin.payout.dto.response.PayoutSummaryResponse;
import ccm.admin.payout.entity.Payout;
import ccm.admin.payout.entity.enums.PayoutStatus;
import ccm.admin.payout.repository.PayoutRepository;
import ccm.admin.payout.service.PayoutAdminService;
import ccm.admin.payout.spec.PayoutSpecification;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutAdminServiceImpl implements PayoutAdminService {

    private final PayoutRepository payoutRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutSummaryResponse> listPayouts(
            Long userId,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        log.debug("Listing payouts - userId: {}, status: {}, fromDate: {}, toDate: {}",
                userId, status, fromDate, toDate);

        // Build dynamic specification
        Specification<Payout> spec = Specification.allOf();

        if (userId != null) {
            spec = spec.and(PayoutSpecification.hasUserId(userId));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and(PayoutSpecification.hasStatus(status));
        }

        if (fromDate != null) {
            spec = spec.and(PayoutSpecification.hasRequestedDateFrom(fromDate));
        }

        if (toDate != null) {
            spec = spec.and(PayoutSpecification.hasRequestedDateTo(toDate));
        }

        Page<Payout> payoutPage = payoutRepository.findAll(spec, pageable);
        return payoutPage.map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutDetailResponse getPayoutDetail(Long id) {
        log.debug("Getting payout detail for ID: {}", id);

        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Payout not found with id: " + id));

        return mapToDetailResponse(payout);
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutStatisticsResponse getPayoutStatistics() {
        log.debug("Calculating payout statistics");

        long total = payoutRepository.getTotalCount();
        long pending = payoutRepository.countByStatus(PayoutStatus.PENDING);
        long approved = payoutRepository.countByStatus(PayoutStatus.APPROVED);
        long rejected = payoutRepository.countByStatus(PayoutStatus.REJECTED);
        long completed = payoutRepository.countByStatus(PayoutStatus.COMPLETED);

        BigDecimal totalAmount = payoutRepository.calculateTotalAmountRequested();
        BigDecimal approvedAmount = payoutRepository.calculateTotalAmountByStatus(PayoutStatus.APPROVED);
        BigDecimal completedAmount = payoutRepository.calculateTotalAmountByStatus(PayoutStatus.COMPLETED);

        double approvalRate = total > 0 ? ((double) (approved + completed) / total) * 100.0 : 0.0;

        return PayoutStatisticsResponse.builder()
                .totalPayouts(total)
                .pendingPayouts(pending)
                .approvedPayouts(approved)
                .rejectedPayouts(rejected)
                .completedPayouts(completed)
                .totalAmountRequested(totalAmount)
                .totalAmountApproved(approvedAmount)
                .totalAmountCompleted(completedAmount)
                .approvalRate(approvalRate)
                .build();
    }

    @Override
    @Transactional
    public PayoutDetailResponse approvePayout(Long id, Long adminId, UpdatePayoutStatusRequest request) {
        log.info("Admin {} approving payout {}", adminId, id);

        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Payout not found with id: " + id));

        // Validate status transition
        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payouts can be approved. Current status: " + payout.getStatus());
        }

        // Update payout
        payout.setStatus(PayoutStatus.APPROVED);
        payout.setProcessedAt(LocalDateTime.now());
        payout.setProcessedBy(adminId);
        payout.setNotes(request != null ? request.getNotes() : null);

        Payout savedPayout = payoutRepository.save(payout);

        log.info("Payout {} approved by admin {}", id, adminId);
        
        // AuditInterceptor will automatically log this change
        return mapToDetailResponse(savedPayout);
    }

    @Override
    @Transactional
    public PayoutDetailResponse rejectPayout(Long id, Long adminId, UpdatePayoutStatusRequest request) {
        log.info("Admin {} rejecting payout {}", adminId, id);

        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Payout not found with id: " + id));

        // Validate status transition
        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payouts can be rejected. Current status: " + payout.getStatus());
        }

        // Update payout
        payout.setStatus(PayoutStatus.REJECTED);
        payout.setProcessedAt(LocalDateTime.now());
        payout.setProcessedBy(adminId);
        payout.setNotes(request != null ? request.getNotes() : "Rejected by admin");

        Payout savedPayout = payoutRepository.save(payout);

        log.info("Payout {} rejected by admin {}", id, adminId);
        
        // AuditInterceptor will automatically log this change
        return mapToDetailResponse(savedPayout);
    }

    /** Map entity to summary response */
    private PayoutSummaryResponse mapToSummaryResponse(Payout payout) {
        String userEmail = getUserEmail(payout.getUserId());

        return PayoutSummaryResponse.builder()
                .id(payout.getId())
                .userId(payout.getUserId())
                .userEmail(userEmail)
                .amount(payout.getAmount())
                .status(payout.getStatus())
                .paymentMethod(payout.getPaymentMethod())
                .requestedAt(payout.getRequestedAt())
                .processedAt(payout.getProcessedAt())
                .build();
    }

    /** Map entity to detail response */
    private PayoutDetailResponse mapToDetailResponse(Payout payout) {
        String userEmail = getUserEmail(payout.getUserId());
        String processedByEmail = payout.getProcessedBy() != null ? getUserEmail(payout.getProcessedBy()) : null;

        return PayoutDetailResponse.builder()
                .id(payout.getId())
                .userId(payout.getUserId())
                .userEmail(userEmail)
                .amount(payout.getAmount())
                .status(payout.getStatus())
                .paymentMethod(payout.getPaymentMethod())
                .bankAccount(payout.getBankAccount())
                .requestedAt(payout.getRequestedAt())
                .processedAt(payout.getProcessedAt())
                .processedBy(payout.getProcessedBy())
                .processedByEmail(processedByEmail)
                .notes(payout.getNotes())
                .build();
    }

    /** Get user email by ID */
    private String getUserEmail(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }
}
