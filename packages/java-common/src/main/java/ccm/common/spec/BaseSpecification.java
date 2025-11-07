package ccm.common.spec;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base utility class for building reusable JPA Specifications.
 * Provides common filtering patterns: keyword search, equality, date ranges, etc.
 * 
 * <p>Usage example:
 * <pre>
 * Specification&lt;User&gt; spec = BaseSpecification.&lt;User&gt;builder()
 *     .keyword("john", "email", "fullName")
 *     .equals("status", AccountStatus.ACTIVE)
 *     .dateRange("createdAt", startDate, endDate)
 *     .build();
 * </pre>
 * 
 * @param <T> Entity type
 */
public class BaseSpecification<T> {

    private final List<Specification<T>> specifications = new ArrayList<>();

    /**
     * Start building a specification
     */
    public static <T> BaseSpecification<T> builder() {
        return new BaseSpecification<>();
    }

    /**
     * Build the final combined specification
     */
    public Specification<T> build() {
        if (specifications.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        
        Specification<T> result = specifications.get(0);
        for (int i = 1; i < specifications.size(); i++) {
            result = result.and(specifications.get(i));
        }
        return result;
    }

    /**
     * Add a custom specification
     */
    public BaseSpecification<T> add(Specification<T> spec) {
        if (spec != null) {
            specifications.add(spec);
        }
        return this;
    }

    /**
     * Keyword search across multiple fields (case-insensitive LIKE)
     * 
     * @param keyword Search term
     * @param fields Field names to search
     * @return this for chaining
     */
    public BaseSpecification<T> keyword(String keyword, String... fields) {
        if (keyword == null || keyword.isBlank() || fields.length == 0) {
            return this;
        }

        specifications.add((root, query, cb) -> {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            Predicate[] predicates = new Predicate[fields.length];
            
            for (int i = 0; i < fields.length; i++) {
                predicates[i] = cb.like(cb.lower(getPath(root, fields[i])), pattern);
            }
            
            return cb.or(predicates);
        });
        
        return this;
    }

    /**
     * Equality filter
     * 
     * @param field Field name (supports nested: "role.name")
     * @param value Value to compare
     * @return this for chaining
     */
    public BaseSpecification<T> equals(String field, Object value) {
        if (value == null) {
            return this;
        }

        specifications.add((root, query, cb) -> 
            cb.equal(getPath(root, field), value)
        );
        
        return this;
    }

    /**
     * Equality filter for enum from string
     * 
     * @param field Field name
     * @param value String value to convert to enum
     * @param enumClass Enum class
     * @return this for chaining
     */
    public <E extends Enum<E>> BaseSpecification<T> enumEquals(String field, String value, Class<E> enumClass) {
        if (value == null || value.isBlank()) {
            return this;
        }

        try {
            E enumValue = Enum.valueOf(enumClass, value.toUpperCase());
            return equals(field, enumValue);
        } catch (IllegalArgumentException e) {
            // Invalid enum value, ignore this filter
            return this;
        }
    }

    /**
     * LIKE filter (case-insensitive)
     * 
     * @param field Field name
     * @param value Value to search
     * @return this for chaining
     */
    public BaseSpecification<T> like(String field, String value) {
        if (value == null || value.isBlank()) {
            return this;
        }

        specifications.add((root, query, cb) -> {
            String pattern = "%" + value.trim().toLowerCase() + "%";
            return cb.like(cb.lower(getPath(root, field)), pattern);
        });
        
        return this;
    }

    /**
     * Date range filter (inclusive)
     * 
     * @param field Field name
     * @param start Start date (null = no lower bound)
     * @param end End date (null = no upper bound)
     * @return this for chaining
     */
    public BaseSpecification<T> dateRange(String field, LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return this;
        }

        specifications.add((root, query, cb) -> {
            Expression<LocalDate> fieldExpr = getPath(root, field);
            
            if (start != null && end != null) {
                return cb.between(fieldExpr, start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(fieldExpr, start);
            } else {
                return cb.lessThanOrEqualTo(fieldExpr, end);
            }
        });
        
        return this;
    }

    /**
     * DateTime range filter (inclusive)
     * 
     * @param field Field name
     * @param start Start datetime (null = no lower bound)
     * @param end End datetime (null = no upper bound)
     * @return this for chaining
     */
    public BaseSpecification<T> dateTimeRange(String field, LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return this;
        }

        specifications.add((root, query, cb) -> {
            Expression<LocalDateTime> fieldExpr = getPath(root, field);
            
            if (start != null && end != null) {
                return cb.between(fieldExpr, start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(fieldExpr, start);
            } else {
                return cb.lessThanOrEqualTo(fieldExpr, end);
            }
        });
        
        return this;
    }

    /**
     * Fetch join to avoid N+1 query problem
     * 
     * @param association Association name (e.g., "role", "owner")
     * @param joinType Join type (LEFT, INNER, RIGHT)
     * @return this for chaining
     */
    public BaseSpecification<T> fetch(String association, JoinType joinType) {
        specifications.add((root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                root.fetch(association, joinType);
            }
            return cb.conjunction();
        });
        
        return this;
    }

    /**
     * Helper to get nested path (e.g., "role.name" → root.get("role").get("name"))
     */
    @SuppressWarnings("unchecked")
    private <Y> Path<Y> getPath(Root<T> root, String field) {
        String[] parts = field.split("\\.");
        Path<?> path = root;
        
        for (String part : parts) {
            path = path.get(part);
        }
        
        return (Path<Y>) path;
    }

    /**
     * Combine two specifications with AND
     */
    public static <T> Specification<T> and(Specification<T> a, Specification<T> b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.and(b);
    }

    /**
     * Combine two specifications with OR
     */
    public static <T> Specification<T> or(Specification<T> a, Specification<T> b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.or(b);
    }
}
