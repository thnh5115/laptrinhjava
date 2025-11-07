package ccm.admin.user.repository;

import ccm.admin.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** repository - Service Interface - repository business logic and data operations */

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
