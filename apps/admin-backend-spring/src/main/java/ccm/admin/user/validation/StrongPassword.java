package ccm.admin.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Password must contain at least one uppercase, one lowercase, one digit, and one special character (!@#$%^&*)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
