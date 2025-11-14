package ccm.buyer.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record DirectBuyRequest(
    @NotNull Long buyerId,
    @NotNull Long listingId,
    @NotNull @Positive BigDecimal qty
) {}
