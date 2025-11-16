package ccm.admin.user.service;

import ccm.admin.user.dto.request.CreateUserRequest;
import ccm.admin.user.dto.response.*;
import ccm.admin.user.entity.enums.AccountStatus;
import java.util.List;

/** service - Service Interface - service business logic and data operations */

public interface UserService {
    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserResponse create(CreateUserRequest req);
    UserResponse suspend(Long id);
    UserResponse updateStatus(Long id, AccountStatus newStatus);
    UserResponse updateRole(Long id, String roleName);
    void delete(Long id);
}
