package ccm.cva.verification.presentation;

import ccm.common.dto.paging.PageResponse;
import ccm.cva.verification.application.command.ApproveVerificationRequestCommand;
import ccm.cva.verification.application.command.CreateVerificationRequestCommand;
import ccm.cva.verification.application.command.RejectVerificationRequestCommand;
import ccm.cva.verification.application.service.VerificationService;
import ccm.cva.verification.domain.VerificationRequest;
import ccm.cva.verification.presentation.dto.ApproveVerificationRequestPayload;
import ccm.cva.verification.presentation.dto.CreateVerificationRequestPayload;
import ccm.cva.verification.presentation.dto.RejectVerificationRequestPayload;
import ccm.cva.verification.presentation.dto.VerificationRequestResponse;
import ccm.cva.verification.presentation.mapper.VerificationRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cva/requests")
@Tag(name = "CVA Requests", description = "Verification lifecycle management")
public class VerificationRequestController {

    private final VerificationService service;
    private final VerificationRequestMapper mapper;

    public VerificationRequestController(
            VerificationService service,
            VerificationRequestMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Create a verification request")
    @PostMapping
    public ResponseEntity<VerificationRequestResponse> create(@Valid @RequestBody CreateVerificationRequestPayload payload) {
        CreateVerificationRequestCommand command = new CreateVerificationRequestCommand(
            payload.ownerId(),
            payload.tripId(),
            payload.distanceKm(),
            payload.energyKwh(),
            payload.checksum(),
            payload.notes()
        );

        VerificationRequest created = service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @Operation(summary = "List verification requests")
    @GetMapping
    public PageResponse<VerificationRequestResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        Page<VerificationRequest> page = service.findAll(pageable);
        List<VerificationRequestResponse> content = page.getContent().stream()
            .map(mapper::toResponse)
            .toList();
        return new PageResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            pageable.getSort().toString()
        );
    }

    @Operation(summary = "Get verification request details")
    @GetMapping("/{id}")
    public VerificationRequestResponse detail(@PathVariable UUID id) {
        VerificationRequest request = service.get(id);
        return mapper.toResponse(request);
    }

    @Operation(summary = "Approve a verification request")
    @PutMapping("/{id}/approve")
    public ResponseEntity<VerificationRequestResponse> approve(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody ApproveVerificationRequestPayload payload
    ) {
        String resolvedIdempotencyKey = (idempotencyKey != null && !idempotencyKey.isBlank())
            ? idempotencyKey
            : payload.idempotencyKey();
        String resolvedCorrelationId = (correlationId != null && !correlationId.isBlank())
            ? correlationId
            : payload.correlationId();

        ApproveVerificationRequestCommand command = new ApproveVerificationRequestCommand(
            payload.verifierId(),
            payload.notes(),
            resolvedIdempotencyKey,
            resolvedCorrelationId
        );

        VerificationRequest updated = service.approve(id, command);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapper.toResponse(updated));
    }

    @Operation(summary = "Reject a verification request")
    @PutMapping("/{id}/reject")
    public ResponseEntity<VerificationRequestResponse> reject(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody RejectVerificationRequestPayload payload
    ) {
        // correlationId reserved for future audit pipeline
        RejectVerificationRequestCommand command = new RejectVerificationRequestCommand(
            payload.verifierId(),
            payload.reason()
        );

        VerificationRequest updated = service.reject(id, command);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapper.toResponse(updated));
    }
}
