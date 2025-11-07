package ccm.cva.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cvaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Carbon Verification Authority API")
                .description("APIs for managing carbon verification requests and issuance lifecycle")
                .version("v1")
                .contact(new Contact()
                    .name("Carbon Credit Marketplace Team")
                    .email("support@carbon.local"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
            .externalDocs(new ExternalDocumentation()
                .description("Module documentation")
                .url("https://github.com/thnh5115/laptrinhjava"));
    }
}
