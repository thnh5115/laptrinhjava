package ccm.admin.user.controller;

import ccm.admin.user.dto.request.CreateUserRequest;
import ccm.admin.user.dto.response.*;
import ccm.admin.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user-management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
/** User - REST Controller - Admin endpoints for User management */

public class UserController {

    private final UserService userService;

    /** GET /api/admin/user-management - perform operation */
    @GetMapping
    public List<UserResponse> all() {
        return userService.findAll();
    }

    /** POST /api/admin/user-management - create new record */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        var created = userService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/suspend")
    public UserResponse suspend(@PathVariable("id") Long id) {
        return userService.suspend(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
