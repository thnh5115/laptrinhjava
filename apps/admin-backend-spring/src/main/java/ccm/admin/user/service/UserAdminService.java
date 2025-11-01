package ccm.admin.user.service;

import ccm.admin.user.dto.response.UserSummaryResponse;
import ccm.common.dto.paging.PageResponse;
import ccm.admin.user.entity.User;
import ccm.admin.user.entity.enums.AccountStatus;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.user.spec.UserSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> searchUsers(
            Integer page, Integer size, String sort,
            String role, String status, String keyword
    ) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 200) ? 20 : size;

        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(p, s, sortSpec);

        AccountStatus statusEnum = parseStatus(status);
        Specification<User> spec = Specification.where(UserSpecification.fetchRole())  // ← FIX N+1
                .and(UserSpecification.statusEquals(statusEnum))
                .and(UserSpecification.roleEquals(role))
                .and(UserSpecification.keywordLike(keyword));

        Page<User> pageData = userRepository.findAll(spec, pageable);

        var content = pageData.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageResponse<>(
                content,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isFirst(),
                pageData.isLast(),
                (sort == null ? "" : sort)
        );
    }

    private UserSummaryResponse toDto(User u) {
        return new UserSummaryResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole() == null ? null : u.getRole().getName(),
                u.getStatus() == null ? null : u.getStatus().name(),
                u.getCreatedAt()
        );
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        var parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private AccountStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
