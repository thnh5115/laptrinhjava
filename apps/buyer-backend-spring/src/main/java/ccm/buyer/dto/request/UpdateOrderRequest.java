package ccm.buyer.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    @Min(1)
    private Double amount;

    @Min(0)
    private Double price;
}