package ccm.owner.auth.service;

import ccm.owner.auth.dto.request.OwnerRegistrationRequest;
import ccm.owner.auth.dto.response.OwnerRegistrationResponse;
import ccm.owner.wallet.entity.EWallet;
import ccm.owner.wallet.entity.enums.WalletStatus;
import ccm.owner.wallet.repository.EWalletRepository;
import ccm.admin.user.entity.Role;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.RoleRepository;
import ccm.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Service for EV Owner registration
 */
public class OwnerRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EWalletRepository eWalletRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new EV Owner
     */
    @Transactional
    public OwnerRegistrationResponse registerOwner(OwnerRegistrationRequest request) {
        log.info("Processing EV Owner registration: email={}", request.getEmail());

        // 1. Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 2. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    String.format("Email '%s' is already registered. Please use a different email address or login.",
                            request.getEmail()));
        }

        // 3. Get EV_OWNER role
        Role evOwnerRole = roleRepository.findByName("EV_OWNER")
                .orElseThrow(() -> new IllegalStateException("EV_OWNER role not found in system"));

        // 4. Create user account
        User newUser = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(AccountStatus.ACTIVE)
                .role(evOwnerRole)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User account created: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 5. Create e-wallet for the user
        EWallet wallet = EWallet.builder()
                .userId(savedUser.getId())
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .status(WalletStatus.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();

        EWallet savedWallet = eWalletRepository.save(wallet);
        log.info("E-Wallet created: id={}, userId={}, balance=${}",
                savedWallet.getId(), savedWallet.getUserId(), savedWallet.getBalance());

        // 6. Build response
        return OwnerRegistrationResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(evOwnerRole.getName())
                .status(savedUser.getStatus().name())
                .createdAt(savedUser.getCreatedAt())
                .walletId(savedWallet.getId())
                .walletCurrency(savedWallet.getCurrency())
                .message("Registration successful! Welcome to Carbon Credit Marketplace.")
                .nextSteps("Please login with your credentials to start submitting EV journeys and earning carbon credits.")
                .build();
    }

    /**
     * Check if email is available for registration
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Validate registration request
     */
    public void validateRegistration(OwnerRegistrationRequest request) {
        // Email validation
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Password match validation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Terms acceptance validation
        if (!Boolean.TRUE.equals(request.getAcceptTerms())) {
            throw new IllegalArgumentException("You must accept the terms and conditions");
        }
    }
}