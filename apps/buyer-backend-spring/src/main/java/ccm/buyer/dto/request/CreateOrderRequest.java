package ccm.buyer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull
    private Long buyerId;

    @NotNull @Min(1)
    private Double amount;

    @NotNull @Min(0)
    private Double price;
}