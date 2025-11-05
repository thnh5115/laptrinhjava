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
import ccm.cva.issuance.domain.CreditIssuance;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DefaultVerificationService implements VerificationService {

    private final VerificationRequestRepository repository;
    private final ValidationEngine validationEngine;
    private final IssuanceService issuanceService;
    private final AuditLogClient auditLogClient;

    public DefaultVerificationService(
            VerificationRequestRepository repository,
            ValidationEngine validationEngine,
            IssuanceService issuanceService,
            AuditLogClient auditLogClient
    ) {
        this.repository = repository;
        this.validationEngine = validationEngine;
        this.issuanceService = issuanceService;
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
        String trimmedTripId = command.tripId() != null ? command.tripId().trim() : null;
        request.setTripId(trimmedTripId);
        request.setDistanceKm(command.distanceKm());
        request.setEnergyKwh(command.energyKwh());
        request.setChecksum(command.checksum() != null ? command.checksum().trim() : null);
        request.setNotes(command.notes());
        request.setStatus(VerificationStatus.PENDING);
        request.setCreatedAt(Instant.now());
        VerificationRequest saved = repository.save(request);

        Map<String, Object> auditPayload = new HashMap<>();
        auditPayload.put("requestId", saved.getId());
        auditPayload.put("ownerId", saved.getOwnerId());
        auditPayload.put("event", "CREATED");
        auditPayload.put("distanceKm", saved.getDistanceKm());
        auditPayload.put("energyKwh", saved.getEnergyKwh());
        auditLogClient.record("cva.request.created", auditPayload);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VerificationRequest> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationRequest get(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Verification request %s not found".formatted(id)));
    }

    @Override
    @Transactional
    public VerificationRequest approve(UUID id, ApproveVerificationRequestCommand command) {
        if (!StringUtils.hasText(command.idempotencyKey())) {
            throw new DomainValidationException(
                "Missing idempotency key",
                List.of("Provide X-Idempotency-Key header or idempotencyKey in payload")
            );
        }

        VerificationRequest request = get(id);
        Optional<CreditIssuance> existingIssuance = issuanceService.getByIdempotencyKey(command.idempotencyKey());
        if (existingIssuance.isPresent()) {
            CreditIssuance issuance = existingIssuance.get();
            if (!issuance.getVerificationRequest().getId().equals(request.getId())) {
                throw new DomainValidationException(
                    "Idempotency key already used",
                    List.of("Provided idempotency key belongs to another request")
                );
            }
            if (request.getStatus() != VerificationStatus.APPROVED) {
                request.setStatus(VerificationStatus.APPROVED);
                request.setVerifiedAt(Instant.now());
            }
            request.setVerifierId(command.verifierId());
            if (StringUtils.hasText(command.notes())) {
                request.setNotes(command.notes());
            }
            return repository.save(request);
        }

        ensurePending(request);

        CreditIssuance issuance = issuanceService.issueCredits(request, command.idempotencyKey(), command.correlationId());

        request.setStatus(VerificationStatus.APPROVED);
        request.setVerifiedAt(Instant.now());
        request.setVerifierId(command.verifierId());
        if (StringUtils.hasText(command.notes())) {
            request.setNotes(command.notes());
        }

        Map<String, Object> auditPayload = new HashMap<>();
        auditPayload.put("requestId", request.getId());
        auditPayload.put("ownerId", request.getOwnerId());
        auditPayload.put("verifierId", command.verifierId());
        auditPayload.put("event", "APPROVED");
        auditPayload.put("issuanceId", issuance.getId());
        auditPayload.put("creditsRounded", issuance.getCreditsRounded());
        auditPayload.put("correlationId", command.correlationId());
        auditLogClient.record("cva.request.approved", auditPayload);

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

        Map<String, Object> auditPayload = new HashMap<>();
        auditPayload.put("requestId", request.getId());
        auditPayload.put("ownerId", request.getOwnerId());
        auditPayload.put("verifierId", command.verifierId());
        auditPayload.put("event", "REJECTED");
        auditPayload.put("reason", command.reason());
        auditLogClient.record("cva.request.rejected", auditPayload);

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
