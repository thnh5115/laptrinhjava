package ccm.buyer.dto.request;

import jakarta.validation.constraints.*;

public record DirectBuyRequest(
    @NotNull Long buyerId,
    @NotNull Long listingId,
    @NotNull @Positive Integer qty
) {}
