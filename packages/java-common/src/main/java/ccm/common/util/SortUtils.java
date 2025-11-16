package ccm.common.util;

import org.springframework.data.domain.Sort;

/**
 * Utility class for parsing sort parameters in API endpoints.
 * Standardizes sort parameter format: "field,direction" (e.g., "createdAt,desc")
 */
public class SortUtils {

    /**
     * Parses a combined sort string into a Spring Data Sort object.
     * 
     * Format: "field,direction" where direction is "asc" or "desc" (case-insensitive)
     * Default direction: DESC
     * Default field: "createdAt"
     * 
     * Examples:
     * - "createdAt,desc" → Sort by createdAt descending
     * - "name,asc" → Sort by name ascending
     * - "status" → Sort by status descending (default)
     * - null or blank → Sort by createdAt descending (fallback)
     * 
     * @param sort Combined sort string in "field,direction" format
     * @return Sort object ready for PageRequest
     */
    public static Sort parseSort(String sort) {
        return parseSort(sort, "createdAt");
    }

    /**
     * Parses a combined sort string with a custom default field.
     * 
     * @param sort Combined sort string in "field,direction" format
     * @param defaultField Field to use if sort is null/blank
     * @return Sort object ready for PageRequest
     */
    public static Sort parseSort(String sort, String defaultField) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, defaultField);
        }
        
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        return Sort.by(direction, field);
    }

    /**
     * Validates if a sort field is allowed.
     * Useful for preventing injection or unsupported fields.
     * 
     * @param field Field name to validate
     * @param allowedFields Array of allowed field names
     * @return true if field is in allowedFields (case-sensitive)
     */
    public static boolean isValidField(String field, String... allowedFields) {
        if (field == null || field.isBlank()) return false;
        for (String allowed : allowedFields) {
            if (allowed.equals(field)) return true;
        }
        return false;
    }
}
