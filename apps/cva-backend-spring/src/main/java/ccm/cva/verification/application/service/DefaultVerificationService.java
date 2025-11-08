package ccm.cva.verification.application.service;

import ccm.cva.audit.client.AuditLogClient;
import ccm.cva.issuance.application.service.IssuanceService;
import ccm.cva.shared.exception.DomainValidationException;
import ccm.cva.shared.exception.ResourceNotFoundException;
import ccm.cva.shared.outbox.AuditOutboxPayload;
import ccm.cva.shared.outbox.OutboxService;
import ccm.cva.shared.trace.CorrelationIdHolder;
import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.command.RejectVerificationRequestCommand;
import ccm.cva.verification.application.query.VerificationRequestQuery;
import ccm.cva.verification.application.query.VerificationRequestSpecifications;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DefaultVerificationService implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultVerificationService.class);

    private final VerificationRequestRepository repository;
    private final ValidationEngine validationEngine;
    private final IssuanceService issuanceService;
    private final AuditLogClient auditLogClient;
    private final OutboxService outboxService;
    private final RetryTemplate externalRetryTemplate;

    public DefaultVerificationService(
            VerificationRequestRepository repository,
            ValidationEngine validationEngine,
            IssuanceService issuanceService,
            AuditLogClient auditLogClient,
            OutboxService outboxService,
            RetryTemplate externalRetryTemplate
    ) {
        this.repository = repository;
        this.validationEngine = validationEngine;
        this.issuanceService = issuanceService;
        this.auditLogClient = auditLogClient;
        this.outboxService = outboxService;
        this.externalRetryTemplate = externalRetryTemplate;
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
        String trimmedNotes = command.notes() != null ? command.notes().trim() : null;
        request.setNotes(StringUtils.hasText(trimmedNotes) ? trimmedNotes : null);
        request.setStatus(VerificationStatus.PENDING);
        request.setCreatedAt(Instant.now());
        VerificationRequest saved = repository.save(request);

        MDC.put("vrId", saved.getId().toString());
        try {
            Map<String, Object> auditPayload = new HashMap<>();
            auditPayload.put("requestId", saved.getId());
            auditPayload.put("ownerId", saved.getOwnerId());
            auditPayload.put("event", "CREATED");
            auditPayload.put("tripId", saved.getTripId());
            auditPayload.put("checksum", saved.getChecksum());
            auditPayload.put("distanceKm", saved.getDistanceKm());
            auditPayload.put("energyKwh", saved.getEnergyKwh());
            if (StringUtils.hasText(saved.getNotes())) {
                auditPayload.put("notes", saved.getNotes());
            }
            recordAuditEvent("cva.request.created", auditPayload);
            log.info("Verification request {} created for owner {}", saved.getId(), saved.getOwnerId());
        } finally {
            MDC.remove("vrId");
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VerificationRequest> search(VerificationRequestQuery query, Pageable pageable) {
        Page<VerificationRequest> page = repository.findAll(
            VerificationRequestSpecifications.fromQuery(query),
            pageable
        );
        page.getContent().forEach(request -> {
            if (request.getCreditIssuance() != null) {
                request.getCreditIssuance().getCreditsRounded();
            }
        });
        return page;
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
        String idempotencyKey = command.idempotencyKey() != null ? command.idempotencyKey().trim() : null;
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new DomainValidationException(
                "Missing idempotency key",
                List.of("Provide X-Idempotency-Key header or idempotencyKey in payload")
            );
        }

        String correlationId = command.correlationId();
        if (correlationId != null) {
            correlationId = correlationId.trim();
            if (!StringUtils.hasText(correlationId)) {
                correlationId = null;
            }
        }

        String trimmedNotes = command.notes() != null ? command.notes().trim() : null;

        VerificationRequest request = get(id);
        Optional<CreditIssuance> existingIssuance = issuanceService.getByIdempotencyKey(idempotencyKey);
        MDC.put("vrId", request.getId().toString());
        if (command.verifierId() != null) {
            MDC.put("verifierId", command.verifierId().toString());
        }
        try {
            if (existingIssuance.isPresent()) {
                CreditIssuance issuance = existingIssuance.get();
                if (!issuance.getVerificationRequest().getId().equals(request.getId())) {
                    throw new DomainValidationException(
                        "Idempotency key already used",
                        List.of("Provided idempotency key belongs to another request")
                    );
                }
                request.setCreditIssuance(issuance);
                if (request.getStatus() != VerificationStatus.APPROVED) {
                    request.setStatus(VerificationStatus.APPROVED);
                    request.setVerifiedAt(Instant.now());
                }
                request.setVerifierId(command.verifierId());
                if (StringUtils.hasText(trimmedNotes)) {
                    request.setNotes(trimmedNotes);
                }
                log.info("Verification request {} approve replay via idempotency key {}", request.getId(), idempotencyKey);
                return repository.save(request);
            }

            ensurePending(request);

            CreditIssuance issuance = issuanceService.issueCredits(request, idempotencyKey, correlationId);
            request.setCreditIssuance(issuance);

            request.setStatus(VerificationStatus.APPROVED);
            request.setVerifiedAt(Instant.now());
            request.setVerifierId(command.verifierId());
            if (StringUtils.hasText(trimmedNotes)) {
                request.setNotes(trimmedNotes);
            }

            Map<String, Object> auditPayload = new HashMap<>();
            auditPayload.put("requestId", request.getId());
            auditPayload.put("ownerId", request.getOwnerId());
            auditPayload.put("verifierId", command.verifierId());
            auditPayload.put("event", "APPROVED");
            auditPayload.put("issuanceId", issuance.getId());
            auditPayload.put("checksum", request.getChecksum());
            auditPayload.put("tripId", request.getTripId());
            auditPayload.put("distanceKm", request.getDistanceKm());
            auditPayload.put("energyKwh", request.getEnergyKwh());
            auditPayload.put("creditsRaw", issuance.getCreditsRaw());
            auditPayload.put("co2ReducedKg", issuance.getCo2ReducedKg());
            auditPayload.put("creditsRounded", issuance.getCreditsRounded());
            auditPayload.put("idempotencyKey", idempotencyKey);
            if (correlationId != null) {
                auditPayload.put("correlationId", correlationId);
            }
            if (StringUtils.hasText(trimmedNotes)) {
                auditPayload.put("notes", trimmedNotes);
            }
            recordAuditEvent("cva.request.approved", auditPayload);
            log.info("Verification request {} approved by {}", request.getId(), command.verifierId());

            return repository.save(request);
        } finally {
            MDC.remove("vrId");
            MDC.remove("verifierId");
        }
    }

    @Override
    @Transactional
    public VerificationRequest reject(UUID id, RejectVerificationRequestCommand command) {
        VerificationRequest request = get(id);
        ensurePending(request);

        String trimmedReason = command.reason() != null ? command.reason().trim() : null;

        request.setStatus(VerificationStatus.REJECTED);
        request.setVerifiedAt(Instant.now());
        request.setVerifierId(command.verifierId());
        request.setNotes(trimmedReason);

        MDC.put("vrId", request.getId().toString());
        if (command.verifierId() != null) {
            MDC.put("verifierId", command.verifierId().toString());
        }
        try {
            Map<String, Object> auditPayload = new HashMap<>();
            auditPayload.put("requestId", request.getId());
            auditPayload.put("ownerId", request.getOwnerId());
            auditPayload.put("verifierId", command.verifierId());
            auditPayload.put("event", "REJECTED");
            auditPayload.put("reason", trimmedReason);
            auditPayload.put("tripId", request.getTripId());
            auditPayload.put("checksum", request.getChecksum());
            auditPayload.put("distanceKm", request.getDistanceKm());
            auditPayload.put("energyKwh", request.getEnergyKwh());
            recordAuditEvent("cva.request.rejected", auditPayload);
            log.info("Verification request {} rejected by {}", request.getId(), command.verifierId());
        } finally {
            MDC.remove("vrId");
            MDC.remove("verifierId");
        }

        return repository.save(request);
    }

    private void ensurePending(VerificationRequest request) {
        if (request.getStatus() != VerificationStatus.PENDING) {
            List<String> errors = new ArrayList<>();
            errors.add("Request already processed with status " + request.getStatus());
            throw new DomainValidationException("Invalid status transition", errors);
        }
    }

    private void recordAuditEvent(String action, Map<String, Object> payload) {
        try {
            externalRetryTemplate.execute(context -> {
                auditLogClient.record(action, payload);
                return null;
            });
        } catch (Exception ex) {
            String correlationId = CorrelationIdHolder.get().orElse(null);
            outboxService.enqueueAuditEvent(new AuditOutboxPayload(action, payload), correlationId);
            log.warn("Audit event {} deferred to outbox: {}", action, ex.getMessage());
        }
    }
}
