package ccm.admin.user.service;

import ccm.admin.user.dto.request.CreateUserRequest;
import ccm.admin.user.entity.Role;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.RoleRepository;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl
 * Tests PR-3 fixes: USER-002 (improved duplicate email error message)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role buyerRole;
    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        buyerRole = new Role();
        buyerRole.setId(1L);
        buyerRole.setName("BUYER");

        validRequest = new CreateUserRequest(
                "newuser@carbon.local",
                "New User",
                "SecurePass123!",
                "BUYER",
                null
        );
    }

    // ========================================
    // USER-002: IMPROVED DUPLICATE EMAIL ERROR MESSAGE TESTS
    // ========================================

    @Test
    @DisplayName("USER-002: Should throw exception with email address when email already exists")
    void testDuplicateEmailErrorMessage() {
        // Given: Email already exists in database
        String duplicateEmail = "existing@carbon.local";
        CreateUserRequest request = new CreateUserRequest(
                duplicateEmail,
                "Some User",
                "password123",
                "BUYER",
                null
        );

        when(userRepo.existsByEmail(duplicateEmail)).thenReturn(true);

        // When & Then: Should throw IllegalArgumentException with specific email in message
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email '" + duplicateEmail + "' is already registered")
                .hasMessageContaining("Please use a different email address");

        // Verify no save operation was attempted
        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("USER-002: Should show helpful error message for duplicate admin email")
    void testDuplicateAdminEmailErrorMessage() {
        // Given: Admin email already exists
        String adminEmail = "admin@carbon.local";
        CreateUserRequest request = new CreateUserRequest(
                adminEmail,
                "Another Admin",
                "admin123",
                "ADMIN",
                null
        );

        when(userRepo.existsByEmail(adminEmail)).thenReturn(true);

        // When & Then: Error message should include the exact email
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email '" + adminEmail + "' is already registered");

        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("USER-002: Should create user successfully when email is unique")
    void testCreateUserWithUniqueEmail() {
        // Given: Email does not exist
        when(userRepo.existsByEmail(validRequest.email())).thenReturn(false);
        when(roleRepo.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode(validRequest.password())).thenReturn("$2a$10$hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .email(validRequest.email())
                .fullName(validRequest.fullName())
                .passwordHash("$2a$10$hashedPassword")
                .status(AccountStatus.ACTIVE)
                .role(buyerRole)
                .build();

        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        // When: Create user
        var response = userService.create(validRequest);

        // Then: Should succeed and return UserResponse
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(validRequest.email());
        assertThat(response.getRole()).isEqualTo("BUYER");

        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("USER-002: Should handle email with special characters in error message")
    void testDuplicateEmailWithSpecialCharacters() {
        // Given: Email with special characters already exists
        String specialEmail = "user+test@carbon.local";
        CreateUserRequest request = new CreateUserRequest(
                specialEmail,
                "User",
                "password",
                "BUYER",
                null
        );

        when(userRepo.existsByEmail(specialEmail)).thenReturn(true);

        // When & Then: Error message should properly include special characters
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email '" + specialEmail + "' is already registered");
    }

    @Test
    @DisplayName("Should throw error when role not found")
    void testCreateUserWithInvalidRole() {
        // Given: Role does not exist
        when(userRepo.existsByEmail(validRequest.email())).thenReturn(false);
        when(roleRepo.findByName("BUYER")).thenReturn(Optional.empty());

        // When & Then: Should throw IllegalArgumentException
        assertThatThrownBy(() -> userService.create(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found: BUYER");

        verify(userRepo, never()).save(any());
    }
}
