package ccm.admin.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing tokens and user information")
/** response - Response DTO - Response model for response data */

public class AuthResponse {
    
    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "Token type (always 'Bearer')", example = "Bearer", defaultValue = "Bearer")
    private String tokenType;
    
    @Schema(description = "Access token expiration time in seconds", example = "900")
    private Long expiresIn;
    
    @Schema(description = "Refresh token for obtaining new access tokens", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
    
    @Schema(description = "Authenticated user information")
    private UserInfo user;

    public AuthResponse() {
        this.tokenType = "Bearer"; 
    }

    public AuthResponse(String accessToken, Long expiresIn, String refreshToken, UserInfo user) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    
    @Schema(description = "User information")
    public static class UserInfo {
        @Schema(description = "User ID", example = "1")
        private Long id;
        
        @Schema(description = "User email address", example = "admin@gmail.com")
        private String email;
        
        @Schema(description = "User full name", example = "Admin User")
        private String fullName;
        
        @Schema(description = "User role", example = "ADMIN")
        private String role;
        
        @Schema(description = "User account status", example = "ACTIVE")
        private String status;

        public UserInfo() {}

        public UserInfo(Long id, String email, String fullName, String role, String status) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
