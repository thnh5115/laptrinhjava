package ccm.admin.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User Overview Response DTO
 * Comprehensive view of user's activity and stats (READ-ONLY)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOverviewResponse {
    
    // Basic user info
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // Activity statistics
    private Long listingCount;           // Total listings created by user (OWNER)
    private Long transactionCount;       // Total transactions (buyer or seller)
    private Long disputeCount;           // Total disputes involved
    private Long journeyCount;           // Total journeys created (OWNER)
    
    // Financial statistics
    private BigDecimal walletBalance;    // Current wallet balance (from payouts)
    private BigDecimal totalCreditsGenerated;  // Total carbon credits from journeys
    private BigDecimal totalEarnings;    // Total earnings from sales
    private BigDecimal totalSpending;    // Total spending on purchases
    
    // Payout statistics (OWNER only)
    private Long payoutRequestCount;     // Total payout requests
    private BigDecimal pendingPayoutAmount;   // Pending payout requests
    private BigDecimal approvedPayoutAmount;  // Approved payouts
}
