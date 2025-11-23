package ccm.owner.wallet.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {
    private Long ownerId; // ID của người nhận (Owner)
    private BigDecimal amount; // Số tín chỉ/tiền cần cộng
    private String correlationId; // Để truy vết log
}