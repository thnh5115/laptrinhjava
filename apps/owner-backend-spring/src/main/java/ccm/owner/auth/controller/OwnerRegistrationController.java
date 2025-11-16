package ccm.owner.auth.controller;

import ccm.owner.auth.dto.request.OwnerRegistrationRequest;
import ccm.owner.auth.dto.response.OwnerRegistrationResponse;
import ccm.owner.auth.service.OwnerRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/owner")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "EV Owner - Registration", description = "Public registration endpoints for EV Owners")
/**
 * Controller for EV Owner registration (Public access)
 */
public class OwnerRegistrationController {

    private final OwnerRegistrationService registrationService;

    @Operation(
            summary = "Register as EV Owner",
            description = "Create a new account for EV Owner. No authentication required. " +
                    "Automatically creates user account and e-wallet."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Registration successful - Account and wallet created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - Invalid input or email already registered"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<OwnerRegistrationResponse> registerOwner(
            @Valid @RequestBody OwnerRegistrationRequest request) {

        log.info("Received EV Owner registration request: email={}", request.getEmail());

        // Validate registration
        registrationService.validateRegistration(request);

        // Register user
        OwnerRegistrationResponse response = registrationService.registerOwner(request);

        log.info("EV Owner registration completed: userId={}, email={}",
                response.getUserId(), response.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Check Email Availability",
            description = "Check if an email address is available for registration"
    )
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(
            @RequestParam String email) {

        log.debug("Checking email availability: {}", email);

        boolean available = registrationService.isEmailAvailable(email);

        return ResponseEntity.ok(Map.of(
                "email", email,
                "available", available,
                "message", available
                        ? "Email is available for registration"
                        : "Email is already registered"
        ));
    }

    @Operation(
            summary = "Get Registration Requirements",
            description = "Get password requirements and registration rules"
    )
    @GetMapping("/requirements")
    public ResponseEntity<Map<String, Object>> getRegistrationRequirements() {
        return ResponseEntity.ok(Map.of(
                "passwordRequirements", Map.of(
                        "minLength", 8,
                        "requireUppercase", true,
                        "requireLowercase", true,
                        "requireDigit", true,
                        "requireSpecialChar", true,
                        "allowedSpecialChars", "!@#$%^&*",
                        "commonPasswordsBlocked", true
                ),
                "emailRequirements", Map.of(
                        "format", "Valid email format required",
                        "maxLength", 255,
                        "uniqueness", "Email must not be already registered"
                ),
                "nameRequirements", Map.of(
                        "minLength", 2,
                        "maxLength", 120
                ),
                "termsRequired", true,
                "accountCreation", Map.of(
                        "autoCreateWallet", true,
                        "initialBalance", 0.00,
                        "currency", "USD",
                        "role", "EV_OWNER",
                        "status", "ACTIVE"
                )
        ));
    }

    @Operation(
            summary = "Validate Registration Data",
            description = "Pre-validate registration data before submitting. Useful for frontend validation."
    )
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateRegistration(
            @Valid @RequestBody OwnerRegistrationRequest request) {

        try {
            registrationService.validateRegistration(request);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Registration data is valid"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }
}