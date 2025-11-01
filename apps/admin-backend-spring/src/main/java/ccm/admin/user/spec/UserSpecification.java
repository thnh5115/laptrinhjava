package ccm.admin.user.spec;

import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    /**
     * Fetch role eagerly to avoid N+1 query problem
     */
    public static Specification<User> fetchRole() {
        return (root, query, cb) -> {
            if (query != null) {
                root.fetch("role", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<User> statusEquals(AccountStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> roleEquals(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.isBlank()) return null;
            var role = root.join("role", JoinType.LEFT);
            return cb.equal(cb.upper(role.get("name")), roleName.toUpperCase());
        };
    }

    public static Specification<User> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("email")), like),
                cb.like(cb.lower(root.get("fullName")), like)
            );
        };
    }
}
