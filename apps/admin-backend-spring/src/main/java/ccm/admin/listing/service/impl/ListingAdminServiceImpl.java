package ccm.admin.listing.service.impl;

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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Listing Admin Service
 * Handles listing management operations for Admin users
 */
@Service
@RequiredArgsConstructor
public class ListingAdminServiceImpl implements ListingAdminService {

    private final ListingRepository listingRepo;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> getAllListings(ListingFilterRequest req, Pageable pageable) {
        // Build specification with filters and eager fetch owner
        Specification<Listing> spec = ListingSpecification.filter(req)
                .and(ListingSpecification.fetchOwner());
        
        // Query with specification
        var page = listingRepo.findAll(spec, pageable);
        
        // Map to DTO
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

    @Override
    @Transactional(readOnly = true)
    public ListingSummaryResponse getListingById(Long id) {
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        return toDTO(listing);
    }

    @Override
    @Transactional
    public ListingSummaryResponse updateListingStatus(Long id, String status) {
        // Validate listing exists
        var listing = listingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + id));
        
        // Validate and convert status
        ListingStatus newStatus;
        try {
            newStatus = ListingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + 
                    ". Must be PENDING, APPROVED, or REJECTED");
        }
        
        // Update status
        listing.setStatus(newStatus);
        listingRepo.save(listing);
        
        return toDTO(listing);
    }

    /**
     * Convert Listing entity to DTO
     * Null-safe to prevent NPE
     */
    private ListingSummaryResponse toDTO(Listing listing) {
        return ListingSummaryResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .ownerEmail(listing.getOwner() != null ? listing.getOwner().getEmail() : null)
                .ownerFullName(listing.getOwner() != null ? listing.getOwner().getFullName() : null)
                .price(listing.getPrice())
                .quantity(listing.getQuantity())
                .unit(listing.getUnit())
                .status(listing.getStatus() != null ? listing.getStatus().name() : null)
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }
}
