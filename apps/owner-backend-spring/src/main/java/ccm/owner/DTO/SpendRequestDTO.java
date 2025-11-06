package ccm.owner.DTO;

import java.math.BigDecimal;

// DTO for the /spend endpoint
public record SpendRequestDTO(
        BigDecimal amount
) {}