package ccm.admin.listing.controller;

import ccm.common.dto.paging.PageResponse;
import ccm.admin.listing.dto.request.ListingFilterRequest;
import ccm.admin.listing.dto.request.ListingStatusUpdateRequest;
import ccm.admin.listing.dto.response.ListingSummaryResponse;
import ccm.admin.listing.service.ListingAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin Listing Management
 * Endpoints for viewing, filtering, and approving/rejecting listings
 */
@RestController
@RequestMapping("/api/admin/listings")
@RequiredArgsConstructor
public class ListingAdminController {

    private final ListingAdminService listingService;

    /**
     * GET /api/admin/listings
     * Get all listings with filtering and pagination
     * 
     * Query params:
     * - page: page number (default 0)
     * - size: page size (default 20, max 200)
     * - sort: sort field,direction (e.g. createdAt,desc)
     * - keyword: search in title
     * - status: filter by status (PENDING, APPROVED, REJECTED)
     * - ownerEmail: filter by owner email
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ListingSummaryResponse>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ownerEmail
    ) {
        // Build filter request
        ListingFilterRequest filterRequest = new ListingFilterRequest();
        filterRequest.setKeyword(keyword);
        filterRequest.setStatus(status);
        filterRequest.setOwnerEmail(ownerEmail);
        
        // Build pageable
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 200);
        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(p, s, sortSpec);
        
        // Query
        PageResponse<ListingSummaryResponse> response = listingService.getAllListings(filterRequest, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/listings/{id}
     * Get listing details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> getListingById(@PathVariable Long id) {
        ListingSummaryResponse response = listingService.getListingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/admin/listings/{id}/status
     * Update listing status (approve/reject)
     * 
     * Request body: {"status": "APPROVED"} or {"status": "REJECTED"}
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> updateListingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ListingStatusUpdateRequest request
    ) {
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        
        ListingSummaryResponse response = listingService.updateListingStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Parse sort parameter
     * Format: field,direction (e.g. createdAt,desc)
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        
        return Sort.by(direction, field);
    }
}
