package ccm.buyer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Data
@Getter
@Setter
@Builder
public class BuyerResponse {
    private Long id;
    private String email;
    private String fullName;
    private String status;
}
