package ccm.cva.config;

import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.util.Objects;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    public DevDataInitializer(
            VerificationRequestRepository verificationRequestRepository,
            CreditIssuanceRepository creditIssuanceRepository
    ) {
        Objects.requireNonNull(verificationRequestRepository, "verificationRequestRepository");
        Objects.requireNonNull(creditIssuanceRepository, "creditIssuanceRepository");
        // Flyway dev migrations now seed deterministic data; keep constructor for context wiring.
    }

    @Override
    public void run(String... args) {
        // Seeding moved to Flyway dev-migration scripts to keep database state deterministic.
    }
}
