package ccm.admin.user.spec;

import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.common.spec.BaseSpecification;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/** spec - JPA Specification - Dynamic query builder for spec filters */

public class UserSpecification {

    
    public static Specification<User> fetchRole() {
        return BaseSpecification.<User>builder()
            .fetch("role", JoinType.LEFT)
            .build();
    }

    
    public static Specification<User> statusEquals(AccountStatus status) {
        return BaseSpecification.<User>builder()
            .equals("status", status)
            .build();
    }

    
    public static Specification<User> roleEquals(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            var role = root.join("role", JoinType.LEFT);
            return cb.equal(cb.upper(role.get("name")), roleName.toUpperCase());
        };
    }

    
    public static Specification<User> keywordLike(String keyword) {
        return BaseSpecification.<User>builder()
            .keyword(keyword, "email", "fullName")
            .build();
    }
}
