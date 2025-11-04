package ccm.buyer.dto.request;

import lombok.Data;

@Data
public class CreateBuyerRequest {
    private String email;
    private String fullName;
    private String password;
    private String status;
}
