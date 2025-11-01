package ccm.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration for Admin Backend API
 * 
 * Provides:
 * - Interactive API documentation via Swagger UI
 * - OpenAPI JSON specification for frontend code generation
 * - JWT Bearer authentication support in Swagger UI
 * 
 * Access Points:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * PR-7: FE Integration Blockers - OpenAPI/Swagger Documentation
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Admin Backend API")
                        .version("1.0.0")
                        .description("""
                                Carbon Credit Marketplace - Admin Module API
                                
                                This API provides comprehensive admin functionality including:
                                - User Management (CRUD, role/status updates)
                                - Transaction Monitoring & Control
                                - Dispute Resolution
                                - Reports & Analytics (with caching)
                                - System Settings Management
                                - Audit Log Access
                                
                                Authentication:
                                - JWT Bearer token required for all admin endpoints
                                - Rate limiting: 5 requests/minute on /api/auth/login
                                - Token expiration: 15 minutes (use /api/auth/refresh to renew)
                                
                                Features:
                                - Pagination on all list endpoints (page, size, sort)
                                - Standardized error responses (ApiError format)
                                - RBAC enforcement (ADMIN role required)
                                - Comprehensive audit logging
                                - Performance optimizations (indexes, caching)
                                """)
                        .contact(new Contact()
                                .name("Admin Backend Team")
                                .email("admin@carbon.local"))
                        .license(new License()
                                .name("Private - Internal Use Only")
                                .url("https://carbon-marketplace.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://admin-api.carbon-marketplace.com")
                                .description("Production Server (change as needed)")
                ))
                // Add JWT Bearer security scheme
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                JWT Bearer Token Authentication
                                                
                                                Steps to authenticate:
                                                1. Click 'Authorize' button below
                                                2. Enter your JWT token (without 'Bearer ' prefix)
                                                3. Click 'Authorize' to apply
                                                
                                                To get a token:
                                                1. Use POST /api/auth/login with credentials:
                                                   { "username": "admin@carbon.local", "password": "Admin@123" }
                                                2. Copy the 'accessToken' from the response
                                                3. Paste it in the 'Authorize' dialog
                                                
                                                Token expires in 15 minutes. Use POST /api/auth/refresh to renew.
                                                """)));
    }

    /**
     * Group API configuration - scan only controller packages
     * This prevents OpenAPI from scanning internal Spring beans
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin-api")
                .packagesToScan(
                    "ccm.admin.admin_backend.controller",
                    "ccm.admin.admin_backend.user.controller",
                    "ccm.admin.admin_backend.auth.controller",
                    "ccm.admin.admin_backend.listing.controller",
                    "ccm.admin.admin_backend.transaction.controller",
                    "ccm.admin.admin_backend.dispute.controller",
                    "ccm.admin.admin_backend.report.controller",
                    "ccm.admin.admin_backend.analytics.controller",
                    "ccm.admin.admin_backend.system.settings.controller",
                    "ccm.admin.admin_backend.audit.controller",
                    "ccm.admin.admin_backend.system"
                )
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * Customize OpenAPI to remove problematic internal schemas
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Remove internal/proxy schemas that cause serialization issues
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().getSchemas().keySet().removeIf(key ->
                    key.startsWith("Hibernate") ||
                    key.startsWith("Spring") ||
                    key.contains("$$") ||
                    key.contains("EnhancerBySpring") ||
                    key.contains("_javassist_")
                );
            }
        };
    }
}
