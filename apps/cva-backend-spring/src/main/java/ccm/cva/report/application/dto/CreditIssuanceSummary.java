package ccm.cva.report.application.dto;

import ccm.admin.credit.entity.enums.CreditStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditIssuanceSummary(
        Long id,
        BigDecimal amount,
        CreditStatus status,
        BigDecimal pricePerCredit,
        LocalDateTime listedAt,
        LocalDateTime soldAt,
        LocalDateTime createdAt
) {}
