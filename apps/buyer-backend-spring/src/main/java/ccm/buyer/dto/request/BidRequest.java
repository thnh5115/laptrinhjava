package ccm.buyer.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record BidRequest(
    @NotNull Long buyerId,
    @NotNull Long auctionId,
    @NotNull @Positive BigDecimal bidPrice
) {}
