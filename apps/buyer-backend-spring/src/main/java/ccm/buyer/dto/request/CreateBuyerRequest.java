package ccm.buyer.dto.request;

import jakarta.validation.constraints.*;

public record CreateBuyerRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 150) String email,
    @PositiveOrZero Double balance
) {}
