package ccm.admin.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation for strong password validation.
 * 
 * <p><b>PR-6 (SEC-001): Password Strength Requirements</b></p>
 * <ul>
 *   <li>Minimum 8 characters (already enforced by @Size)</li>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one lowercase letter</li>
 *   <li>At least one digit</li>
 *   <li>At least one special character (!@#$%^&*)</li>
 *   <li>No common passwords (password123, admin, etc.)</li>
 * </ul>
 * 
 * @see StrongPasswordValidator
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Password must contain at least one uppercase, one lowercase, one digit, and one special character (!@#$%^&*)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
