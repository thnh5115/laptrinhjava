package ccm.admin.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncoderTest {

    @Test
    public void testBCryptPasswordEncoding() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "password123";
        String storedHash = "$2a$10$8K2L0Hkd1DoHgcGEZLpm.2YrttgFYfQY87xsXDi/Jbw2BNdIGQ4W";

        // Test if the stored hash matches the raw password
        boolean matches = encoder.matches(rawPassword, storedHash);
        System.out.println("Password 'password123' matches stored hash: " + matches);

        // Generate correct hash for password123
        String correctHash = encoder.encode(rawPassword);
        System.out.println("Correct BCrypt hash for 'password123': " + correctHash);

        // Test with correct hash
        boolean correctMatches = encoder.matches(rawPassword, correctHash);
        System.out.println("Password 'password123' matches correct hash: " + correctMatches);

        assertTrue(correctMatches, "Password should match the correct BCrypt hash");
    }
}