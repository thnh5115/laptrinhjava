package ccm.buyer.dto.request;

import ccm.buyer.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data  ;

@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
}
