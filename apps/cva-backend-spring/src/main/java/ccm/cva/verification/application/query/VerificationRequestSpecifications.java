package ccm.cva.verification.application.query;

import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class VerificationRequestSpecifications {

    private VerificationRequestSpecifications() {
    }

    public static Specification<VerificationRequest> fromQuery(VerificationRequestQuery query) {
        Specification<VerificationRequest> spec = Specification.allOf();
        if (query == null) {
            return spec;
        }

        if (query.status() != null) {
            // 1. Chuyển đổi Enum sang String khớp với Database (Admin dùng 'VERIFIED' thay vì 'APPROVED')
            String dbStatusValue = query.status().name();
            if (query.status() == VerificationStatus.APPROVED) {
                dbStatusValue = "VERIFIED"; 
            }
            
            // 2. Dùng biến final để lambda expression truy cập được
            String finalValue = dbStatusValue;

            // 3. Tìm theo trường 'statusString' (tên biến trong Entity mới), không phải 'status'
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("statusString"), finalValue));
        }
        if (query.ownerId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("ownerId"), query.ownerId()));
        }
        LocalDateTime createdFrom = query.createdFrom();
        if (createdFrom != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
        }
        LocalDateTime createdTo = query.createdTo();
        if (createdTo != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
        }
        if (StringUtils.hasText(query.search())) {
            String pattern = "%" + query.search().trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                cb.like(root.get("id").as(String.class), pattern), // Tìm theo ID số
                cb.like(cb.lower(root.get("checksum")), pattern)
            ));
        }
        return spec;
    }
}
