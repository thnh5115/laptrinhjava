package ccm.admin.system;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final PasswordEncoder passwordEncoder;

    public TestController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/hash-password")
    public Map<String, String> hashPassword(@RequestBody Map<String, String> req) {
        String plain = req.get("password");
        String hashed = passwordEncoder.encode(plain);
        return Map.of(
            "plaintext", plain,
            "hashed", hashed,
            "matches_test", String.valueOf(passwordEncoder.matches(plain, hashed))
        );
    }

    @PostMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> req) {
        String plain = req.get("password");
        String hash = req.get("hash");
        boolean matches = passwordEncoder.matches(plain, hash);
        return Map.of(
            "plaintext", plain,
            "hash", hash,
            "matches", matches
        );
    }
}
