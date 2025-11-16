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

/** repository - Service Interface - repository business logic and data operations */

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    
    
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findWithRoleByEmail(String email);
    
    boolean existsByEmail(String email);
    List<User> findByStatus(AccountStatus status);
    List<User> findByRole_Name(String roleName);
    
    
    @Override
    @EntityGraph(attributePaths = {"role"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
