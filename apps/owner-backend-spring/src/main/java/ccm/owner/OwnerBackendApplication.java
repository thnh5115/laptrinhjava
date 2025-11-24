package ccm.owner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan(basePackages = {"ccm.owner", "ccm.admin"})
@ComponentScan(basePackages = {"ccm.owner", "ccm.admin"})
@EnableJpaRepositories(basePackages = {"ccm.owner", "ccm.admin"})
public class OwnerBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(OwnerBackendApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Cho phép tất cả đường dẫn
                        .allowedOrigins("http://localhost:3000", "http://localhost:3001") // Cho phép Frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Cho phép các loại lệnh
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}