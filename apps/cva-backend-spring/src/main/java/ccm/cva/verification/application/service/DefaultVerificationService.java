package ccm.cva.verification.application.service;

import ccm.cva.audit.client.AuditLogClient;
import ccm.cva.issuance.application.service.IssuanceService;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.shared.exception.ResourceNotFoundException;
import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.command.RejectVerificationRequestCommand;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.domain.VerificationStatus;
import ccm.cva.verification.infrastructure.repository.VerificationRequestRepository;
import ccm.cva.wallet.client.WalletClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultVerificationService implements VerificationService {

    private final VerificationRequestRepository repository;
    private final ValidationEngine validationEngine;
    @SuppressWarnings("unused")
    private final IssuanceService issuanceService;
    @SuppressWarnings("unused")
    private final WalletClient walletClient;
    @SuppressWarnings("unused")
    private final AuditLogClient auditLogClient;

    public DefaultVerificationService(
            VerificationRequestRepository repository,
            ValidationEngine validationEngine,
            IssuanceService issuanceService,
            WalletClient walletClient,
            AuditLogClient auditLogClient
    ) {
        this.repository = repository;
        this.validationEngine = validationEngine;
        this.issuanceService = issuanceService;
        this.walletClient = walletClient;
        this.auditLogClient = auditLogClient;
    }

    @Override
    @Transactional
    public VerificationRequest create(CreateVerificationRequestCommand command) {
        ValidationResult result = validationEngine.validate(command);
        if (!result.valid()) {
            throw new DomainValidationException("Verification request validation failed", result.messages());
        }

        repository.findByChecksum(command.checksum()).ifPresent(existing -> {
            throw new DomainValidationException(
                "Duplicate submission detected",
                List.of("A verification request with the provided checksum already exists")
            );
        });

        VerificationRequest request = new VerificationRequest();
        request.setOwnerId(command.ownerId());
        request.setTripId(command.tripId());
        request.setDistanceKm(command.distanceKm());
        request.setEnergyKwh(command.energyKwh());
        request.setChecksum(command.checksum());
        request.setNotes(command.notes());
        request.setStatus(VerificationStatus.PENDING);
        request.setCreatedAt(Instant.now());

        return repository.save(request);
    }

    @Override
    public Page<VerificationRequest> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public VerificationRequest get(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Verification request %s not found".formatted(id)));
    }

    @Override
    @Transactional
    public VerificationRequest approve(UUID id, ApproveVerificationRequestCommand command) {
        VerificationRequest request = get(id);
        ensurePending(request);

        request.setStatus(VerificationStatus.APPROVED);
        request.setVerifiedAt(Instant.now());
        request.setVerifierId(command.verifierId());
        request.setNotes(command.notes());
        // TODO Week 2: Trigger issuance workflow and audit trail
        return repository.save(request);
    }

    @Override
    @Transactional
    public VerificationRequest reject(UUID id, RejectVerificationRequestCommand command) {
        VerificationRequest request = get(id);
        ensurePending(request);

        request.setStatus(VerificationStatus.REJECTED);
        request.setVerifiedAt(Instant.now());
        request.setVerifierId(command.verifierId());
        request.setNotes(command.reason());
        // TODO Week 2: Publish audit event
        return repository.save(request);
    }

    private void ensurePending(VerificationRequest request) {
        if (request.getStatus() != VerificationStatus.PENDING) {
            List<String> errors = new ArrayList<>();
            errors.add("Request already processed with status " + request.getStatus());
            throw new DomainValidationException("Invalid status transition", errors);
        }
    }
}
