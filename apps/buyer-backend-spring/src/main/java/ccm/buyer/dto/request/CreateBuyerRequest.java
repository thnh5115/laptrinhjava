package ccm.buyer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class CreateBuyerRequest {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotBlank(message = "Status cannot be empty")
    private String status;
    
}
