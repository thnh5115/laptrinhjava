package ccm.admin.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** request - Request DTO - Request payload for request operations */

public class LoginRequest {
    @NotBlank(message = "Email must not be blank")
    private String email;
    
    @NotBlank(message = "Password must not be blank")
    private String password;

    public LoginRequest() {}                
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
