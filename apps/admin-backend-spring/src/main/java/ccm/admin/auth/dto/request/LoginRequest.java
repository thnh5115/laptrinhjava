package ccm.admin.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;
    
    @NotBlank(message = "Password must not be blank")
    private String password;

    public LoginRequest() {}                // <- no-args constructor BẮT BUỘC CHO JACKSON
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
