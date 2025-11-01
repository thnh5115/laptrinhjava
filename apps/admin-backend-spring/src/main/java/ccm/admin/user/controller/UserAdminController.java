package ccm.admin.user.controller;

import ccm.admin.user.dto.request.UserRoleUpdateRequest;
import ccm.admin.user.dto.request.UserStatusUpdateRequest;
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

    /**
     * GET /api/admin/users/{id}
     * Get user details by ID
     * 
     * @param id User ID
     * @return UserResponse with full user details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/admin/users/{id}/status
     * Update user account status (ACTIVE, SUSPENDED, BANNED)
     * 
     * Request body example:
     * {
     *   "status": "ACTIVE"
     * }
     * 
     * @param id User ID
     * @param req Request with new status value
     * @return Updated UserResponse
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest req) {
        
        // Validate and parse status
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

    /**
     * PUT /api/admin/users/{id}/role
     * Update user role (ADMIN, AUDITOR, BUYER, EV_OWNER)
     * 
     * Request body example:
     * {
     *   "role": "BUYER"
     * }
     * 
     * @param id User ID
     * @param req Request with new role name
     * @param authentication Current authenticated user
     * @return Updated UserResponse
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest req,
            Authentication authentication) {
        
        // Validate role input
        if (req == null || req.role() == null || req.role().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        
        // USER-003 FIX: Prevent admin from removing their own ADMIN role
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Invalid session - user not found"));
        
        // Check if trying to demote self
        if (currentUser.getId().equals(id) && !"ADMIN".equalsIgnoreCase(req.role().trim())) {
            throw new IllegalArgumentException(
                "Cannot remove ADMIN role from yourself. Ask another admin to change your role.");
        }
        
        String roleName = req.role().trim();
        UserResponse updated = userService.updateRole(id, roleName);
        return ResponseEntity.ok(updated);
    }
}
