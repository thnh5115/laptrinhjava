package ccm.admin.dispute.service.impl;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.dispute.dto.request.UpdateDisputeStatusRequest;
import ccm.admin.dispute.dto.response.DisputeDetailResponse;
import ccm.admin.dispute.dto.response.DisputeSummaryResponse;
import ccm.admin.dispute.entity.Dispute;
import ccm.admin.dispute.repository.DisputeRepository;
import ccm.admin.dispute.service.DisputeService;
import ccm.admin.dispute.spec.DisputeSpecification;
import ccm.common.util.SortUtils;
import jakarta.persistence.EntityNotFoundException;
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

@Service
@RequiredArgsConstructor
@Slf4j
/** Dispute - Service Implementation - Business logic for Dispute operations */

public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;

    /** Get all records - transactional */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<DisputeSummaryResponse> getAllDisputes(
            int page,
            int size,
            String sort,
            String keyword,
            String status
    ) {
        log.info("Fetching disputes - page: {}, size: {}, sort: {}, keyword: {}, status: {}",
                page, size, sort, keyword, status);

        
        Sort sortSpec = SortUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        
        Specification<Dispute> spec = DisputeSpecification.filter(keyword, status);
        Page<Dispute> disputePage = disputeRepository.findAll(spec, pageable);

        
        List<DisputeSummaryResponse> content = disputePage.getContent().stream()
                .map(dispute -> DisputeSummaryResponse.builder()
                        .id(dispute.getId())
                        .disputeCode(dispute.getDisputeCode())
                        .raisedBy(dispute.getRaisedBy())
                        .status(dispute.getStatus().name())
                        .transactionId(dispute.getTransactionId())
                        .createdAt(dispute.getCreatedAt())
                        .build())
                .toList();

        log.info("Found {} disputes (total: {})", content.size(), disputePage.getTotalElements());

        return new PageResponse<>(
                content,
                disputePage.getNumber(),
                disputePage.getSize(),
                disputePage.getTotalElements(),
                disputePage.getTotalPages(),
                disputePage.isFirst(),
                disputePage.isLast(),
                sort  
        );
    }

    /** Process business logic - transactional */
    @Override
    @Transactional(readOnly = true)
    public DisputeDetailResponse getDisputeById(Long id) {
        log.info("Fetching dispute details for ID: {}", id);

        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispute not found with ID: " + id));

        DisputeDetailResponse response = DisputeDetailResponse.builder()
                .id(dispute.getId())
                .disputeCode(dispute.getDisputeCode())
                .raisedBy(dispute.getRaisedBy())
                .description(dispute.getDescription())
                .adminNote(dispute.getAdminNote())
                .status(dispute.getStatus().name())
                .transactionId(dispute.getTransactionId())
                .createdAt(dispute.getCreatedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();

        log.info("Dispute details retrieved for code: {}", dispute.getDisputeCode());
        return response;
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {
        "analytics:kpis",
        "analytics:disputes"
    }, allEntries = true)
    /** Update status - modifies data */
    public void updateStatus(Long id, UpdateDisputeStatusRequest request) {
        log.info("Updating dispute status for ID: {} to {}", id, request.getStatus());

        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispute not found with ID: " + id));

        
        dispute.setStatus(request.getStatus());
        dispute.setAdminNote(request.getAdminNote());
        dispute.setUpdatedAt(LocalDateTime.now());

        disputeRepository.save(dispute);

        log.info("Dispute {} status updated to {} by admin", dispute.getDisputeCode(), request.getStatus());
    }
}
