package ccm.admin.listing.service.impl;

import ccm.admin.listing.dto.request.ListingModerationRequest;
import ccm.admin.user.repository.UserRepository;
import ccm.common.dto.paging.PageResponse;
import ccm.admin.listing.dto.request.ListingFilterRequest;
import ccm.admin.listing.dto.response.ListingSummaryResponse;
import ccm.admin.listing.entity.Listing;
import ccm.admin.listing.entity.enums.ListingStatus;
import ccm.admin.listing.repository.ListingRepository;
import ccm.admin.listing.service.ListingAdminService;
import ccm.admin.listing.spec.ListingSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
/** Listing - Service Implementation - Business logic for Listing operations */

public class ListingAdminServiceImpl implements ListingAdminService {

    private final ListingRepository listingRepo;
    private final UserRepository userRepository;

    /** Get all records - transactional */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> getAllListings(ListingFilterRequest req, Pageable pageable) {
        
        Specification<Listing> spec = ListingSpecification.filter(req)
                .and(ListingSpecification.fetchOwner());
        
        
        var page = listingRepo.findAll(spec, pageable);
        
        
        var content = page.getContent().stream()
                .map(this::toDTO)
                .toList();
        
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                pageable.getSort().toString()
        );
    }

    /** Process business logic - transactional */
    @Override
    @Transactional(readOnly = true)
    public ListingSummaryResponse getListingById(Long id) {
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        return toDTO(listing);
    }

    /** Update status - transactional */
    @Override
    @Transactional
    public ListingSummaryResponse updateListingStatus(Long id, String status) {
        
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        
        
        ListingStatus newStatus;
        try {
            newStatus = ListingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + 
                    ". Must be PENDING, APPROVED, or REJECTED");
        }
        
        
        listing.setStatus(newStatus);
        listingRepo.save(listing);
        
        return toDTO(listing);
    }

    /** Approve a listing */
    @Override
    @Transactional
    public ListingSummaryResponse approveListing(Long id, Long adminId) {
        log.info("Admin {} approving listing {}", adminId, id);
        
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        
        // Validate status transition
        if (listing.getStatus() != ListingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING listings can be approved. Current status: " + listing.getStatus());
        }
        
        // Update listing
        listing.setStatus(ListingStatus.APPROVED);
        listing.setApprovedBy(adminId);
        listing.setApprovedAt(LocalDateTime.now());
        listing.setRejectReason(null);  // Clear any previous rejection reason
        
        listingRepo.save(listing);
        
        log.info("Listing {} approved by admin {}", id, adminId);
        
        // AuditInterceptor will automatically log this change
        return toDTO(listing);
    }

    /** Reject a listing */
    @Override
    @Transactional
    public ListingSummaryResponse rejectListing(Long id, Long adminId, ListingModerationRequest request) {
        log.info("Admin {} rejecting listing {}", adminId, id);
        
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        
        // Validate status transition
        if (listing.getStatus() != ListingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING listings can be rejected. Current status: " + listing.getStatus());
        }
        
        // Update listing
        listing.setStatus(ListingStatus.REJECTED);
        listing.setApprovedBy(adminId);
        listing.setApprovedAt(LocalDateTime.now());
        listing.setRejectReason(request != null ? request.getReason() : "Rejected by admin");
        
        listingRepo.save(listing);
        
        log.info("Listing {} rejected by admin {}", id, adminId);
        
        // AuditInterceptor will automatically log this change
        return toDTO(listing);
    }

    /** Delist a listing */
    @Override
    @Transactional
    public ListingSummaryResponse delistListing(Long id, Long adminId, ListingModerationRequest request) {
        log.info("Admin {} delisting listing {}", adminId, id);
        
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        
        // Validate status transition - can only delist APPROVED listings
        if (listing.getStatus() != ListingStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED listings can be delisted. Current status: " + listing.getStatus());
        }
        
        // Update listing
        listing.setStatus(ListingStatus.DELISTED);
        listing.setApprovedBy(adminId);  // Track who delisted
        listing.setApprovedAt(LocalDateTime.now());
        listing.setRejectReason(request != null ? request.getReason() : "Delisted by admin");
        
        listingRepo.save(listing);
        
        log.info("Listing {} delisted by admin {}", id, adminId);
        
        // AuditInterceptor will automatically log this change
        return toDTO(listing);
    }

    
    private ListingSummaryResponse toDTO(Listing listing) {
        String approvedByEmail = listing.getApprovedBy() != null 
            ? userRepository.findById(listing.getApprovedBy())
                .map(user -> user.getEmail())
                .orElse(null)
            : null;
        
        return ListingSummaryResponse.builder()
                .id(listing.getId())
                .carbonCreditId(listing.getCarbonCreditId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .ownerEmail(listing.getOwner() != null ? listing.getOwner().getEmail() : null)
                .ownerFullName(listing.getOwner() != null ? listing.getOwner().getFullName() : null)
                .price(listing.getPrice())
                .quantity(listing.getQuantity())
                .unit(listing.getUnit())
                .listingType(listing.getListingType() != null ? listing.getListingType().name() : null)
                .status(listing.getStatus() != null ? listing.getStatus().name() : null)
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .approvedBy(listing.getApprovedBy())
                .approvedByEmail(approvedByEmail)
                .approvedAt(listing.getApprovedAt())
                .rejectReason(listing.getRejectReason())
                .build();
    }
}
