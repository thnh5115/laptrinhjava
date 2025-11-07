package ccm.admin.user.controller;

import ccm.admin.user.dto.request.UserRoleUpdateRequest;
import ccm.admin.user.dto.request.UserStatusUpdateRequest;
import ccm.admin.user.dto.response.UserOverviewResponse;
import ccm.admin.user.dto.response.UserResponse;
import ccm.admin.user.dto.response.UserSummaryResponse;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.service.UserAdminService;
import ccm.admin.user.service.UserService;
import ccm.common.dto.paging.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
/**
 * User - REST Controller - Admin endpoints for User management
 */

public class UserAdminController {

    private final UserAdminService userAdminService;
    private final UserService userService;
    private final UserRepository userRepository;

    public UserAdminController(UserAdminService userAdminService, UserService userService, UserRepository userRepository) {
        this.userAdminService = userAdminService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserSummaryResponse>> search(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        var result = userAdminService.searchUsers(page, size, sort, role, status, keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserStatusUpdateRequest req) {

        AccountStatus newStatus;
        try {
            if (req == null || req.status() == null || req.status().isBlank()) {
                throw new IllegalArgumentException("Status is required");
            }
            newStatus = AccountStatus.valueOf(req.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Valid values: ACTIVE, SUSPENDED, BANNED");
        }

        UserResponse updated = userService.updateStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRoleUpdateRequest req,
            Authentication authentication) {

        if (req == null || req.role() == null || req.role().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Invalid session - user not found"));

        if (currentUser.getId().equals(id) && !"ADMIN".equalsIgnoreCase(req.role().trim())) {
            throw new IllegalArgumentException(
                    "Cannot remove ADMIN role from yourself. Ask another admin to change your role.");
        }

        String roleName = req.role().trim();
        UserResponse updated = userService.updateRole(id, roleName);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/admin/users/{id}/overview - Get comprehensive user overview
     * Returns aggregated statistics about user's activity (READ-ONLY) Includes:
     * listings, transactions, disputes, journeys, wallet, payouts
     */
    @GetMapping("/{id}/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserOverviewResponse> getUserOverview(@PathVariable("id") Long id) {
        UserOverviewResponse overview = userAdminService.getUserOverview(id);
        return ResponseEntity.ok(overview);
    }
}
