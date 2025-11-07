package ccm.admin.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.regex.Pattern;

/** validation - Validator - Custom validation logic for validation */

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password123", "123456", "12345678", "qwerty", "abc123",
            "monkey", "1234567", "letmein", "trustno1", "dragon", "baseball",
            "iloveyou", "master", "sunshine", "ashley", "bailey", "passw0rd",
            "shadow", "123123", "654321", "superman", "qazwsx", "michael",
            "football", "admin", "administrator", "welcome", "login", "test"
    );

    
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false; 
        }

        
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password is too common and easily guessable. Please choose a stronger password."
            ).addConstraintViolation();
            return false;
        }

        
        boolean hasUppercase = HAS_UPPERCASE.matcher(password).find();
        boolean hasLowercase = HAS_LOWERCASE.matcher(password).find();
        boolean hasDigit = HAS_DIGIT.matcher(password).find();
        boolean hasSpecial = HAS_SPECIAL.matcher(password).find();

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            
            return false;
        }

        return true;
    }
}
