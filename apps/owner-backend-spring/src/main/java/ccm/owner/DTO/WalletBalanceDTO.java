package ccm.owner.DTO;

import java.math.BigDecimal;

// DTO for returning the wallet's full balance breakdown
public record WalletBalanceDTO(
        BigDecimal totalBalance,
        BigDecimal lockedBalance,
        BigDecimal availableBalance
) {}