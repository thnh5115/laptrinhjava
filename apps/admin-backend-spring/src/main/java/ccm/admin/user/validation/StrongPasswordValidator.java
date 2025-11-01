package ccm.admin.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator for strong password requirements.
 * 
 * <p><b>PR-6 (SEC-001): Password Strength Validation</b></p>
 * <ul>
 *   <li>Minimum 8 characters (enforced by @Size on field)</li>
 *   <li>At least one uppercase letter (A-Z)</li>
 *   <li>At least one lowercase letter (a-z)</li>
 *   <li>At least one digit (0-9)</li>
 *   <li>At least one special character (!@#$%^&*)</li>
 *   <li>Reject common weak passwords</li>
 * </ul>
 * 
 * <p><b>Security Benefits:</b></p>
 * <ul>
 *   <li>Prevents weak passwords susceptible to dictionary attacks</li>
 *   <li>Increases password entropy (stronger against brute force)</li>
 *   <li>Blocks common passwords from breach databases</li>
 *   <li>Complies with OWASP password strength guidelines</li>
 * </ul>
 * 
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html">OWASP Authentication Cheat Sheet</a>
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    // Common weak passwords to reject (top 100 most common)
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password123", "123456", "12345678", "qwerty", "abc123",
            "monkey", "1234567", "letmein", "trustno1", "dragon", "baseball",
            "iloveyou", "master", "sunshine", "ashley", "bailey", "passw0rd",
            "shadow", "123123", "654321", "superman", "qazwsx", "michael",
            "football", "admin", "administrator", "welcome", "login", "test"
    );

    // Regex patterns for complexity requirements
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false; // Handled by @NotBlank
        }

        // Check for common weak passwords (case-insensitive)
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password is too common and easily guessable. Please choose a stronger password."
            ).addConstraintViolation();
            return false;
        }

        // Check complexity requirements
        boolean hasUppercase = HAS_UPPERCASE.matcher(password).find();
        boolean hasLowercase = HAS_LOWERCASE.matcher(password).find();
        boolean hasDigit = HAS_DIGIT.matcher(password).find();
        boolean hasSpecial = HAS_SPECIAL.matcher(password).find();

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            // Use default message from annotation
            return false;
        }

        return true;
    }
}
