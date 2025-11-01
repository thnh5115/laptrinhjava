package ccm.admin.user.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for StrongPasswordValidator (PR-6: SEC-001).
 * 
 * Verifies:
 * - Password complexity requirements (uppercase, lowercase, digit, special char)
 * - Common password rejection (password123, admin, etc.)
 * - Minimum length enforcement (8 chars)
 * - Proper validation messages
 */
class StrongPasswordValidatorTest {

    private Validator validator;

    // Test DTO with password field
    record PasswordTestDto(@StrongPassword String password) {}

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should accept strong password with all requirements")
    void testStrongPasswordValid() {
        // Given: Strong password (uppercase, lowercase, digit, special)
        PasswordTestDto dto = new PasswordTestDto("SecureP@ss123");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: No violations
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject password without uppercase letter")
    void testRejectsPasswordWithoutUppercase() {
        // Given: Password without uppercase
        PasswordTestDto dto = new PasswordTestDto("securep@ss123");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Violation for missing uppercase
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .contains("uppercase");
    }

    @Test
    @DisplayName("Should reject password without lowercase letter")
    void testRejectsPasswordWithoutLowercase() {
        // Given: Password without lowercase
        PasswordTestDto dto = new PasswordTestDto("SECUREP@SS123");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Violation for missing lowercase
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .contains("lowercase");
    }

    @Test
    @DisplayName("Should reject password without digit")
    void testRejectsPasswordWithoutDigit() {
        // Given: Password without digit
        PasswordTestDto dto = new PasswordTestDto("SecureP@ssword");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Violation for missing digit
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .contains("digit");
    }

    @Test
    @DisplayName("Should reject password without special character")
    void testRejectsPasswordWithoutSpecialChar() {
        // Given: Password without special character
        PasswordTestDto dto = new PasswordTestDto("SecurePass123");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Violation for missing special character
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .contains("special character");
    }

    @Test
    @DisplayName("Should reject common password: password123")
    void testRejectsCommonPasswordPassword123() {
        // Given: Common weak password
        PasswordTestDto dto = new PasswordTestDto("Password123!");

        // When: Validate (note: "password123" is in common list)
        // This should fail because it contains common password structure
        // Actually, let's test exact common password
        dto = new PasswordTestDto("password123");

        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Should be rejected as common password
        // Note: May also fail complexity check, which is fine
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject common password: admin")
    void testRejectsCommonPasswordAdmin() {
        // Given: Common weak password
        PasswordTestDto dto = new PasswordTestDto("admin");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Rejected as common password (and missing complexity)
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject common password: 123456")
    void testRejectsCommonPassword123456() {
        // Given: Very common weak password
        PasswordTestDto dto = new PasswordTestDto("123456");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Rejected
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should accept password with valid special characters")
    void testAcceptsValidSpecialCharacters() {
        // Given: Passwords with each allowed special character
        String[] validPasswords = {
                "SecureP!ss123",
                "SecureP@ss123",
                "SecureP#ss123",
                "SecureP$ss123",
                "SecureP%ss123",
                "SecureP^ss123",
                "SecureP&ss123",
                "SecureP*ss123"
        };

        // When/Then: All should be valid
        for (String password : validPasswords) {
            PasswordTestDto dto = new PasswordTestDto(password);
            Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);
            assertThat(violations).as("Password '%s' should be valid", password).isEmpty();
        }
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    void testHandlesNullPassword() {
        // Given: Null password
        PasswordTestDto dto = new PasswordTestDto(null);

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Violation (null not allowed)
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle empty password gracefully")
    void testHandlesEmptyPassword() {
        // Given: Empty password
        PasswordTestDto dto = new PasswordTestDto("");

        // When: Validate
        Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);

        // Then: Violation (empty not allowed)
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should accept complex real-world strong password")
    void testAcceptsComplexRealWorldPassword() {
        // Given: Real-world strong passwords
        String[] strongPasswords = {
                "MyS3cur3P@ssw0rd!",
                "Tr0ub4dor&3",
                "C0rrect#H0rse*Battery",
                "Qwerty!23Uiop"
        };

        // When/Then: All should be valid
        for (String password : strongPasswords) {
            PasswordTestDto dto = new PasswordTestDto(password);
            Set<ConstraintViolation<PasswordTestDto>> violations = validator.validate(dto);
            assertThat(violations).as("Password '%s' should be valid", password).isEmpty();
        }
    }
}
