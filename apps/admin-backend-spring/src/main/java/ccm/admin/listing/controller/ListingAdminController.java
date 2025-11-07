package ccm.admin.listing.controller;

import ccm.admin.listing.dto.request.ListingModerationRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/listings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
/**
 * Listing - REST Controller - Admin endpoints for Listing management
 */

public class ListingAdminController {

    private final ListingAdminService listingService;

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

        ListingFilterRequest filterRequest = new ListingFilterRequest();
        filterRequest.setKeyword(keyword);
        filterRequest.setStatus(status);
        filterRequest.setOwnerEmail(ownerEmail);

        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 200);
        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(p, s, sortSpec);

        PageResponse<ListingSummaryResponse> response = listingService.getAllListings(filterRequest, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> getListingById(@PathVariable("id") Long id) {
        ListingSummaryResponse response = listingService.getListingById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> updateListingStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ListingStatusUpdateRequest request
    ) {
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        ListingSummaryResponse response = listingService.updateListingStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Approve a listing PUT /api/admin/listings/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> approveListing(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        // Get admin ID from authentication (assume username is numeric ID for now)
        Long adminId = 1L;  // TODO: Extract from authentication principal

        ListingSummaryResponse response = listingService.approveListing(id, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a listing PUT /api/admin/listings/{id}/reject Body: { "reason":
     * "Inappropriate content" }
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> rejectListing(
            @PathVariable("id") Long id,
            Authentication authentication,
            @RequestBody(required = false) ListingModerationRequest request
    ) {
        // Get admin ID from authentication (assume username is numeric ID for now)
        Long adminId = 1L;  // TODO: Extract from authentication principal

        ListingSummaryResponse response = listingService.rejectListing(id, adminId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delist (remove from marketplace) PUT /api/admin/listings/{id}/delist
     * Body: { "reason": "Violates marketplace policy" }
     */
    @PutMapping("/{id}/delist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingSummaryResponse> delistListing(
            @PathVariable("id") Long id,
            Authentication authentication,
            @RequestBody(required = false) ListingModerationRequest request
    ) {
        // Get admin ID from authentication (assume username is numeric ID for now)
        Long adminId = 1L;  // TODO: Extract from authentication principal

        ListingSummaryResponse response = listingService.delistListing(id, adminId, request);
        return ResponseEntity.ok(response);
    }

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
