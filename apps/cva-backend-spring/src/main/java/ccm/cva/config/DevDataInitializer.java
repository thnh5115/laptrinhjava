package ccm.cva.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Seeding moved to Flyway dev-migration scripts to keep database state deterministic.
    }
}
