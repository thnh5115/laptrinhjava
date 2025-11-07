package ccm.cva.config;

import ccm.cva.issuance.domain.CreditIssuance;
import ccm.cva.issuance.infrastructure.repository.CreditIssuanceRepository;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final VerificationRequestRepository verificationRequestRepository;
    private final CreditIssuanceRepository creditIssuanceRepository;

    public DevDataInitializer(
            VerificationRequestRepository verificationRequestRepository,
            CreditIssuanceRepository creditIssuanceRepository
    ) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.creditIssuanceRepository = creditIssuanceRepository;
    }

    @Override
    public void run(String... args) {
        if (verificationRequestRepository.count() > 0) {
            return;
        }

        UUID verifierId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        UUID ownerA = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID ownerB = UUID.fromString("66666666-7777-8888-9999-aaaaaaaaaaaa");

        Instant now = Instant.now();

        VerificationRequest approvedWeek1 = buildRequest(
            ownerA,
            "TRIP-DEV-001",
            new BigDecimal("125.750"),
            new BigDecimal("42.330"),
            "checksum-dev-001",
            VerificationStatus.APPROVED,
            verifierId,
            now.minus(21, ChronoUnit.DAYS),
            now.minus(20, ChronoUnit.DAYS)
        );

        VerificationRequest rejectedWeek2 = buildRequest(
            ownerB,
            "TRIP-DEV-002",
            new BigDecimal("98.120"),
            new BigDecimal("37.810"),
            "checksum-dev-002",
            VerificationStatus.REJECTED,
            verifierId,
            now.minus(14, ChronoUnit.DAYS),
            now.minus(13, ChronoUnit.DAYS)
        );

        VerificationRequest pendingWeek3 = buildRequest(
            ownerA,
            "TRIP-DEV-003",
            new BigDecimal("85.000"),
            new BigDecimal("29.500"),
            "checksum-dev-003",
            VerificationStatus.PENDING,
            null,
            now.minus(7, ChronoUnit.DAYS),
            null
        );

        List<VerificationRequest> saved = verificationRequestRepository.saveAll(
            List.of(approvedWeek1, rejectedWeek2, pendingWeek3)
        );

        VerificationRequest savedApproved = saved.stream()
            .filter(request -> request.getChecksum().equals("checksum-dev-001"))
            .findFirst()
            .orElseThrow();

        seedIssuance(savedApproved);
    }

    private VerificationRequest buildRequest(
            UUID ownerId,
            String tripId,
            BigDecimal distanceKm,
            BigDecimal energyKwh,
            String checksum,
            VerificationStatus status,
            UUID verifierId,
            Instant createdAt,
            Instant verifiedAt
    ) {
        VerificationRequest request = new VerificationRequest();
        request.setOwnerId(ownerId);
        request.setTripId(tripId);
        request.setDistanceKm(distanceKm);
        request.setEnergyKwh(energyKwh);
        request.setChecksum(checksum);
        request.setStatus(status);
        request.setVerifierId(verifierId);
        request.setCreatedAt(createdAt);
        request.setVerifiedAt(verifiedAt);
        request.setNotes(status == VerificationStatus.REJECTED ? "Emission data incomplete" : "Dev sample record");
        return request;
    }

    private void seedIssuance(VerificationRequest request) {
        CreditIssuance issuance = new CreditIssuance();
        issuance.setVerificationRequest(request);
        issuance.setOwnerId(request.getOwnerId());
        issuance.setCo2ReducedKg(new BigDecimal("456.789000"));
        issuance.setCreditsRaw(new BigDecimal("18.275000"));
        issuance.setCreditsRounded(new BigDecimal("18.28"));
        issuance.setIdempotencyKey("DEV-IDEMPOTENCY-001");
        issuance.setCorrelationId("DEV-CORRELATION-001");
        creditIssuanceRepository.save(issuance);
        request.setCreditIssuance(issuance);
        verificationRequestRepository.save(request);
    }
}
