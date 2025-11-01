package ccm.admin.user.repository;

import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(AccountStatus status);
    List<User> findByRole_Name(String roleName);
    
    /**
     * USER-001 FIX: Override findAll with @EntityGraph to eagerly fetch role
     * This prevents N+1 query problem when loading user list
     */
    @Override
    @EntityGraph(attributePaths = {"role"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
