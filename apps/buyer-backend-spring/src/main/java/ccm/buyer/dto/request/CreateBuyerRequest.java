package ccm.buyer.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateBuyerRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 150) String email,
    @PositiveOrZero BigDecimal balance
) {}
