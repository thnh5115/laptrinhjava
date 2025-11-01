package ccm.admin.user.controller;

import ccm.admin.user.dto.request.UserRoleUpdateRequest;
import ccm.admin.user.dto.response.UserResponse;
import ccm.admin.user.entity.Role;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.service.UserAdminService;
import ccm.admin.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserAdminController
 * Tests PR-3 fixes: USER-003 (self-demotion prevention)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAdminController Tests")
class UserAdminControllerTest {

    @Mock
    private UserAdminService userAdminService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserAdminController controller;

    private User adminUser;
    private User targetUser;
    private Role adminRole;
    private Role buyerRole;

    @BeforeEach
    void setUp() {
        // Create roles
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        buyerRole = new Role();
        buyerRole.setId(2L);
        buyerRole.setName("BUYER");

        // Create admin user (the one performing the action)
        adminUser = User.builder()
                .id(100L)
                .email("admin@carbon.local")
                .fullName("Admin User")
                .status(AccountStatus.ACTIVE)
                .role(adminRole)
                .build();

        // Create target user (different from admin)
        targetUser = User.builder()
                .id(200L)
                .email("user@carbon.local")
                .fullName("Regular User")
                .status(AccountStatus.ACTIVE)
                .role(buyerRole)
                .build();
    }

    // ========================================
    // USER-003: PREVENT SELF-DEMOTION TESTS
    // ========================================

    @Test
    @DisplayName("USER-003: Should prevent admin from removing own ADMIN role")
    void testPreventSelfDemotion() {
        // Given: Admin trying to change their own role to BUYER
        when(authentication.getName()).thenReturn("admin@carbon.local");
        when(userRepository.findByEmail("admin@carbon.local")).thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("BUYER");

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> controller.updateRole(100L, request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot remove ADMIN role from yourself");

        // Verify service was never called
        verify(userService, never()).updateRole(any(), any());
    }

    @Test
    @DisplayName("USER-003: Should prevent admin from changing own role to AUDITOR")
    void testPreventSelfDemotionToAuditor() {
        // Given: Admin trying to change their own role to AUDITOR
        when(authentication.getName()).thenReturn("admin@carbon.local");
        when(userRepository.findByEmail("admin@carbon.local")).thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("AUDITOR");

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> controller.updateRole(100L, request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot remove ADMIN role from yourself");

        verify(userService, never()).updateRole(any(), any());
    }

    @Test
    @DisplayName("USER-003: Should allow admin to change own role if staying as ADMIN")
    void testAllowSelfUpdateIfStayingAdmin() {
        // Given: Admin "updating" their own role to ADMIN (no change)
        when(authentication.getName()).thenReturn("admin@carbon.local");
        when(userRepository.findByEmail("admin@carbon.local")).thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("ADMIN");

        UserResponse expectedResponse = UserResponse.builder()
                .id(100L)
                .email("admin@carbon.local")
                .fullName("Admin User")
                .role("ADMIN")
                .status("ACTIVE")
                .build();

        when(userService.updateRole(100L, "ADMIN")).thenReturn(expectedResponse);

        // When: Update role
        var response = controller.updateRole(100L, request, authentication);

        // Then: Should succeed
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRole()).isEqualTo("ADMIN");

        verify(userService, times(1)).updateRole(100L, "ADMIN");
    }

    @Test
    @DisplayName("USER-003: Should allow admin to change OTHER user's role")
    void testAllowChangingOtherUserRole() {
        // Given: Admin changing a different user's role
        when(authentication.getName()).thenReturn("admin@carbon.local");
        when(userRepository.findByEmail("admin@carbon.local")).thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("BUYER");

        UserResponse expectedResponse = UserResponse.builder()
                .id(200L)
                .email("user@carbon.local")
                .fullName("Regular User")
                .role("BUYER")
                .status("ACTIVE")
                .build();

        when(userService.updateRole(200L, "BUYER")).thenReturn(expectedResponse);

        // When: Update other user's role
        var response = controller.updateRole(200L, request, authentication);

        // Then: Should succeed
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(200L);

        verify(userService, times(1)).updateRole(200L, "BUYER");
    }

    @Test
    @DisplayName("USER-003: Should handle case-insensitive role name when checking self-demotion")
    void testSelfDemotionCaseInsensitive() {
        // Given: Admin trying to change their role to "admin" (lowercase)
        when(authentication.getName()).thenReturn("admin@carbon.local");
        when(userRepository.findByEmail("admin@carbon.local")).thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("admin");

        UserResponse expectedResponse = UserResponse.builder()
                .id(100L)
                .email("admin@carbon.local")
                .role("ADMIN")
                .build();

        when(userService.updateRole(100L, "admin")).thenReturn(expectedResponse);

        // When: Update role (lowercase "admin" should be allowed)
        var response = controller.updateRole(100L, request, authentication);

        // Then: Should succeed (case-insensitive check allows "admin")
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(userService, times(1)).updateRole(100L, "admin");
    }

    @Test
    @DisplayName("USER-003: Should throw exception if current user not found in database")
    void testSelfDemotionWithInvalidSession() {
        // Given: Authentication exists but user not in database (invalid session)
        when(authentication.getName()).thenReturn("nonexistent@carbon.local");
        when(userRepository.findByEmail("nonexistent@carbon.local")).thenReturn(Optional.empty());

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("BUYER");

        // When & Then: Should throw IllegalStateException
        assertThatThrownBy(() -> controller.updateRole(100L, request, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid session - user not found");

        verify(userService, never()).updateRole(any(), any());
    }

    // ========================================
    // VALIDATION TESTS
    // ========================================

    @Test
    @DisplayName("Should reject null role in request")
    void testRejectNullRole() {
        // Given: Request with null role (no mocking needed - validation happens before)
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(null);

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> controller.updateRole(200L, request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role is required");
    }

    @Test
    @DisplayName("Should reject blank role in request")
    void testRejectBlankRole() {
        // Given: Request with blank role (no mocking needed - validation happens before)
        UserRoleUpdateRequest request = new UserRoleUpdateRequest("   ");

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> controller.updateRole(200L, request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role is required");
    }

    @Test
    @DisplayName("Should reject null request body")
    void testRejectNullRequest() {
        // Given: Null request (no mocking needed - validation happens before)

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> controller.updateRole(200L, null, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role is required");
    }

    @Test
    @DisplayName("Should trim whitespace from role name")
    void testTrimRoleName() {
        // Given: Role with leading/trailing whitespace
        when(authentication.getName()).thenReturn("admin@carbon.local");
        when(userRepository.findByEmail("admin@carbon.local")).thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest("  BUYER  ");

        UserResponse expectedResponse = UserResponse.builder()
                .id(200L)
                .role("BUYER")
                .build();

        when(userService.updateRole(200L, "BUYER")).thenReturn(expectedResponse);

        // When: Update role
        controller.updateRole(200L, request, authentication);

        // Then: Should trim and pass "BUYER" to service
        verify(userService, times(1)).updateRole(eq(200L), eq("BUYER"));
    }
}
