package ccm.cva;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.journey.entity.Journey;
import ccm.cva.shared.outbox.OutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "ccm.cva.analytics",
    "ccm.cva.audit",
    "ccm.cva.config",
    "ccm.cva.journey",
    "ccm.cva.report",
    "ccm.cva.security",
    "ccm.cva.shared"
})
@EntityScan(basePackageClasses = {Journey.class, CarbonCredit.class, OutboxEvent.class})
@EnableJpaRepositories(basePackages = {
    "ccm.admin.journey.repository",
    "ccm.admin.credit.repository",
    "ccm.cva.shared.outbox"
})
@EnableScheduling
@EnableRetry
@ConfigurationPropertiesScan("ccm.cva")
public class CvaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvaBackendApplication.class, args);
    }
}
