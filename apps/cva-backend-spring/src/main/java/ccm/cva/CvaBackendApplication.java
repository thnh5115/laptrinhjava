package ccm.cva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@ConfigurationPropertiesScan("ccm.cva")
public class CvaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvaBackendApplication.class, args);
    }
}
