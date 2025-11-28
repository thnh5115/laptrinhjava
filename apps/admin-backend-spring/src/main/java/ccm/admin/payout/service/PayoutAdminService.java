package ccm.admin.payout.service;

import ccm.admin.payout.dto.request.UpdatePayoutStatusRequest;
import ccm.admin.payout.dto.response.PayoutDetailResponse;
import ccm.admin.payout.dto.response.PayoutStatisticsResponse;
import ccm.admin.payout.dto.response.PayoutSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PayoutAdminService {

    /**
     * List payout requests with filtering
     * @param userId Filter by user ID
     * @param status Filter by status (PENDING, APPROVED, REJECTED, COMPLETED)
     * @param fromDate Filter by requested date from
     * @param toDate Filter by requested date to
     * @param pageable Pagination parameters
     * @return Paginated list of payout summaries
     */
    Page<PayoutSummaryResponse> listPayouts(
            Long userId,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );

    /**
     * Get detailed information about a payout request
     * @param id Payout ID
     * @return Payout details
     */
    PayoutDetailResponse getPayoutDetail(Long id);

    /**
     * Get payout statistics
     * @return Aggregated statistics about payouts
     */
    PayoutStatisticsResponse getPayoutStatistics();

    /**
     * Approve a payout request
     * @param id Payout ID
     * @param adminId Admin who is approving
     * @param request Admin notes
     * @return Updated payout details
     */
    PayoutDetailResponse approvePayout(Long id, Long adminId, UpdatePayoutStatusRequest request);

    /**
     * Reject a payout request
     * @param id Payout ID
     * @param adminId Admin who is rejecting
     * @param request Rejection reason
     * @return Updated payout details
     */
    PayoutDetailResponse rejectPayout(Long id, Long adminId, UpdatePayoutStatusRequest request);
    PayoutDetailResponse completePayout(Long id, Long adminId);
}
