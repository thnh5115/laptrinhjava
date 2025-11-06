package ccm.buyer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTransactionRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private Double amount;

    @NotBlank
    private String transactionRef;

}