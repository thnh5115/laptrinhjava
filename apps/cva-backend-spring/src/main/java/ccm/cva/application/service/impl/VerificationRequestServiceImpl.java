package ccm.cva.application.service.impl;

import ccm.cva.application.service.IssuanceService;
import ccm.cva.application.service.ValidationEngine;
import ccm.cva.application.service.VerificationRequestService;
import ccm.cva.application.service.dto.ApproveVerificationRequestCommand;
import ccm.cva.application.service.dto.CreateVerificationRequestCommand;
import ccm.cva.application.service.dto.RejectVerificationRequestCommand;
import ccm.cva.application.service.dto.VerificationRequestDto;
import ccm.cva.domain.exception.VerificationRequestNotFoundException;
import ccm.cva.domain.model.VerificationRequest;
import ccm.cva.infrastructure.client.AuditLogClient;
import ccm.cva.infrastructure.persistence.jpa.VerificationRequestRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationRequestServiceImpl implements VerificationRequestService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final ValidationEngine validationEngine;
    private final IssuanceService issuanceService;
    private final AuditLogClient auditLogClient;

    @Override
    @Transactional
    public VerificationRequestDto create(CreateVerificationRequestCommand command) {
        validationEngine.validateNewRequest(command);

        VerificationRequest request = VerificationRequest.builder()
                .ownerId(command.ownerId())
                .tripId(command.tripId())
                .distanceKm(command.distanceKm())
                .energyKwh(command.energyKwh())
                .checksum(command.checksum())
                .notes(command.notes())
                .build();

    VerificationRequest saved = verificationRequestRepository.save(request);
    auditLogClient.recordVerificationCreated(saved);
    return mapToDto(saved);
    }

    @Override
    public Page<VerificationRequestDto> findAll(Pageable pageable) {
        return verificationRequestRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Override
    public VerificationRequestDto findById(UUID id) {
        VerificationRequest request = verificationRequestRepository.findById(id)
                .orElseThrow(() -> new VerificationRequestNotFoundException(id));
        return mapToDto(request);
    }

    @Override
    @Transactional
    public VerificationRequestDto approve(UUID id, ApproveVerificationRequestCommand command) {
        VerificationRequest request = getPendingRequest(id);
        OffsetDateTime approvedAt = OffsetDateTime.now(ZoneOffset.UTC);
        request.markApproved(command.verifierId(), approvedAt, command.notes());

    // Week 1 skeleton: leave issuance orchestration as a no-op (logged for visibility)
    issuanceService.prepareIssuance(request);
    auditLogClient.recordVerificationDecision(request, command.correlationId());

        return mapToDto(request);
    }

    @Override
    @Transactional
    public VerificationRequestDto reject(UUID id, RejectVerificationRequestCommand command) {
        VerificationRequest request = getPendingRequest(id);
        OffsetDateTime rejectedAt = OffsetDateTime.now(ZoneOffset.UTC);
    request.markRejected(command.verifierId(), rejectedAt, command.reason());
    auditLogClient.recordVerificationDecision(request, null);
        return mapToDto(request);
    }

    private VerificationRequest getPendingRequest(UUID id) {
        VerificationRequest request = verificationRequestRepository.findById(id)
                .orElseThrow(() -> new VerificationRequestNotFoundException(id));
        if (!request.isPending()) {
            throw new IllegalStateException("Verification request is not in PENDING state");
        }
        return request;
    }

    private VerificationRequestDto mapToDto(VerificationRequest request) {
        return new VerificationRequestDto(
                request.getId(),
                request.getOwnerId(),
                request.getTripId(),
                request.getDistanceKm(),
                request.getEnergyKwh(),
                request.getChecksum(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getVerifiedAt(),
                request.getVerifierId(),
                request.getNotes()
        );
    }
}
