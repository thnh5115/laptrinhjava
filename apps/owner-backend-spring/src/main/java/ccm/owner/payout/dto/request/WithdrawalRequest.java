package ccm.owner.payout.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Request DTO for withdrawal (payout request)
 */
public class WithdrawalRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.00", message = "Minimum withdrawal amount is $10.00")
    @DecimalMax(value = "100000.00", message = "Maximum withdrawal amount is $100,000.00")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(BANK_TRANSFER|PAYPAL|CRYPTO)$",
            message = "Payment method must be BANK_TRANSFER, PAYPAL, or CRYPTO")
    private String paymentMethod;

    @NotBlank(message = "Bank account details are required")
    @Size(max = 255, message = "Bank account details must not exceed 255 characters")
    private String bankAccount;

    /**
     * Optional notes for the withdrawal request
     */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}