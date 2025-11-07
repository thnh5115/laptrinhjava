package ccm.admin.user.service.impl;

import ccm.admin.user.dto.request.CreateUserRequest;
import ccm.admin.user.dto.response.*;
import ccm.admin.user.entity.*;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.RoleRepository;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
/** User - Service Implementation - Business logic for User operations */

/** @summary <business action> */

public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    
    private UserResponse toDTO(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole() != null ? u.getRole().getName() : null)
                .status(u.getStatus() != null ? u.getStatus().name() : null)
                .build();
    }

    /** Get all records - transactional */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepo.findAll().stream().map(this::toDTO).toList();
    }

    /** Find by ID - transactional */
    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        var u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toDTO(u);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "analytics:kpis", allEntries = true)
    /** Create new record - modifies data */
    public UserResponse create(CreateUserRequest req) {
        
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException(
                String.format("Email '%s' is already registered. Please use a different email address.", req.email()));
        }
        var role = roleRepo.findByName(req.role())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + req.role()));

        var u = User.builder()
                .email(req.email())
                .fullName(req.fullName())
                .passwordHash(passwordEncoder.encode(req.password()))
                .status(AccountStatus.ACTIVE)
                .role(role)
                .build();

        userRepo.save(u);
        return toDTO(u);
    }

    /** Process business logic - transactional */
    @Override
    @Transactional
    public UserResponse suspend(Long id) {
        var u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        u.setStatus(AccountStatus.SUSPENDED);
        userRepo.save(u);
        return toDTO(u);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "analytics:kpis", allEntries = true)
    /** Update status - modifies data */
    public UserResponse updateStatus(Long id, AccountStatus newStatus) {
        var u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        u.setStatus(newStatus);
        userRepo.save(u);
        return toDTO(u);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "analytics:kpis", allEntries = true)
    /** Update user role - modifies data */
    public UserResponse updateRole(Long id, String roleName) {
        var u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        var role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        u.setRole(role);
        userRepo.save(u);
        return toDTO(u);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "analytics:kpis", allEntries = true)
    /** Delete record - deletes from DB */
    public void delete(Long id) {
        if (!userRepo.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepo.deleteById(id);
    }
}
