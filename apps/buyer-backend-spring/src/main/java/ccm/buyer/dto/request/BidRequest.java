package ccm.buyer.dto.request;

import jakarta.validation.constraints.*;

public record BidRequest(
    @NotNull Long buyerId,
    @NotNull Long auctionId,
    @NotNull @Positive Double bidPrice
) {}
