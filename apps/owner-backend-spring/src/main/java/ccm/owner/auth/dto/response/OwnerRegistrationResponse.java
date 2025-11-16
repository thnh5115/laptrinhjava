package ccm.owner.auth.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Response DTO for EV Owner registration
 */
public class OwnerRegistrationResponse {

    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private String message;

    // Wallet information
    private Long walletId;
    private String walletCurrency;

    // Next steps
    private String nextSteps;
}