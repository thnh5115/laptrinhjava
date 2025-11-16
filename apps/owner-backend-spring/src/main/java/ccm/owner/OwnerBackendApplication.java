package ccm.owner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan(basePackages = {"ccm.owner", "ccm.admin"})
@ComponentScan(basePackages = {"ccm.owner", "ccm.admin"})
@EnableJpaRepositories(basePackages = {"ccm.owner", "ccm.admin"})
public class OwnerBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(OwnerBackendApplication.class, args);
    }
}