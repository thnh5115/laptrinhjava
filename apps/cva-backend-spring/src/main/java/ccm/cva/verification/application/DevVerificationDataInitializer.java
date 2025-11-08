package ccm.cva.verification.application;

import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.command.RejectVerificationRequestCommand;
import ccm.cva.verification.application.service.VerificationService;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds predictable verification requests for the developer profile so the CVA workspace has data out
 * of the box on day one. The initializer is deliberately idempotent and will back off when data already
 * exists to avoid interfering with manual testing.
 */
@Component
@Profile("dev")
public class DevVerificationDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevVerificationDataInitializer.class);

    private static final UUID OWNER_ALPHA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OWNER_BRAVO = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OWNER_CHARLIE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID VERIFIER_ID = UUID.fromString("99999999-aaaa-bbbb-cccc-dddddddddddd");

    private final VerificationService verificationService;
    private final VerificationRequestRepository requestRepository;

    public DevVerificationDataInitializer(
            VerificationService verificationService,
            VerificationRequestRepository requestRepository
    ) {
        this.verificationService = verificationService;
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (requestRepository.count() > 0) {
            log.debug("Skipping dev data seeding because verification_requests already contains rows");
            return;
        }

        log.info("Seeding CVA verification requests for developer profile");

        List<SeedRequest> seeds = List.of(
            new SeedRequest(OWNER_ALPHA, "EV-TRIP-001", 42.6, 8.2, "City commute", SeedDisposition.APPROVE),
            new SeedRequest(OWNER_BRAVO, "EV-TRIP-002", 15.0, 3.4, "Short errand", SeedDisposition.REJECT),
            new SeedRequest(OWNER_CHARLIE, "EV-TRIP-003", 120.0, 24.0, "Highway journey", SeedDisposition.PENDING)
        );

        for (SeedRequest seed : seeds) {
            try {
                VerificationRequest request = createRequest(seed);
                applyDisposition(request, seed.disposition());
            } catch (Exception ex) {
                log.warn("Failed to seed verification request for owner {}: {}", seed.ownerId(), ex.getMessage(), ex);
            }
        }
    }

    private VerificationRequest createRequest(SeedRequest seed) {
        String checksum = sha256(seed.ownerId() + seed.tripId());
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            seed.ownerId(),
            seed.tripId(),
            BigDecimal.valueOf(seed.distanceKm()),
            BigDecimal.valueOf(seed.energyKwh()),
            checksum,
            seed.notes()
        );
        return verificationService.create(command);
    }

    private void applyDisposition(VerificationRequest request, SeedDisposition disposition) {
        switch (disposition) {
            case APPROVE -> {
                String idempotencyKey = "seed-approve-" + request.getId();
                ApproveVerificationRequestCommand approveCommand = new ApproveVerificationRequestCommand(
                    VERIFIER_ID,
                    "Auto-approved for developer sandbox",
                    idempotencyKey,
                    "seed-correlation-" + request.getId()
                );
                verificationService.approve(request.getId(), approveCommand);
            }
            case REJECT -> {
                RejectVerificationRequestCommand rejectCommand = new RejectVerificationRequestCommand(
                    VERIFIER_ID,
                    "Sample data: energy readings inconsistent"
                );
                verificationService.reject(request.getId(), rejectCommand);
            }
            case PENDING -> {
                // leave as pending so the queue UI has work to action
            }
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }

    private record SeedRequest(UUID ownerId, String tripId, double distanceKm, double energyKwh, String notes,
                                SeedDisposition disposition) { }

    private enum SeedDisposition {
        APPROVE,
        REJECT,
        PENDING
    }
}
