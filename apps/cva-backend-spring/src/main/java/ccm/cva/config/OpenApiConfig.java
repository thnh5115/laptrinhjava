package ccm.cva.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cvaOpenAPI(@Value("${spring.application.name:cva-backend}") String applicationName) {
        return new OpenAPI()
                .info(new Info()
                        .title("Carbon Verification Authority API")
                        .description("APIs for CVA services (verification requests, approvals, issuance)")
                        .version("v1")
                        .contact(new Contact()
                                .name("CVA Platform Team")
                                .url("https://github.com/thnh5115/laptrinhjava")
                                .email("cva.support@example.com"))
                        .license(new License().name("Internal Use").url("https://example.com/license"))
                        .summary(applicationName + " OpenAPI documentation"));
    }
}
