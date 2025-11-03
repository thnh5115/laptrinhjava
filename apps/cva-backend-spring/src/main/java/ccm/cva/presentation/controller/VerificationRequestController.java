package ccm.cva.presentation.controller;

import ccm.cva.application.service.VerificationRequestService;
import ccm.cva.application.service.dto.ApproveVerificationRequestCommand;
import ccm.cva.application.service.dto.CreateVerificationRequestCommand;
import ccm.cva.application.service.dto.RejectVerificationRequestCommand;
import ccm.cva.application.service.dto.VerificationRequestDto;
import ccm.cva.presentation.dto.ApproveVerificationRequestPayload;
import ccm.cva.presentation.dto.CreateVerificationRequestPayload;
import ccm.cva.presentation.dto.RejectVerificationRequestPayload;
import ccm.cva.presentation.dto.VerificationRequestResponse;
import ccm.common.dto.paging.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/cva/requests")
@Tag(name = "Verification Requests", description = "Endpoints for managing verification requests")
public class VerificationRequestController {

    private final VerificationRequestService verificationRequestService;

    public VerificationRequestController(VerificationRequestService verificationRequestService) {
        this.verificationRequestService = verificationRequestService;
    }

    @Operation(summary = "Create verification request")
    @PostMapping
    public ResponseEntity<VerificationRequestResponse> create(@Valid @RequestBody CreateVerificationRequestPayload payload) {
        VerificationRequestDto dto = verificationRequestService.create(new CreateVerificationRequestCommand(
                payload.ownerId(),
                payload.tripId(),
                payload.distanceKm(),
                payload.energyKwh(),
                payload.checksum(),
                payload.notes()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(map(dto));
    }

    @Operation(summary = "List verification requests")
    @GetMapping
    public PageResponse<VerificationRequestResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        Page<VerificationRequestDto> page = verificationRequestService.findAll(pageable);
        return new PageResponse<>(
                page.map(this::map).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSort().toString()
        );
    }

    @Operation(summary = "Get verification request detail")
    @GetMapping("/{id}")
    public VerificationRequestResponse getById(@PathVariable UUID id) {
        VerificationRequestDto dto = verificationRequestService.findById(id);
        return map(dto);
    }

    @Operation(summary = "Approve verification request")
    @PutMapping("/{id}/approve")
    public ResponseEntity<VerificationRequestResponse> approve(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveVerificationRequestPayload payload,
            @RequestHeader(name = "X-Idempotency-Key", required = false) String idempotencyKey) {
        VerificationRequestDto dto = verificationRequestService.approve(id, new ApproveVerificationRequestCommand(
                payload.verifierId(),
                payload.notes(),
                idempotencyKey,
                payload.correlationId()
        ));
        return ResponseEntity.accepted().body(map(dto));
    }

    @Operation(summary = "Reject verification request")
    @PutMapping("/{id}/reject")
    public ResponseEntity<VerificationRequestResponse> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectVerificationRequestPayload payload) {
        VerificationRequestDto dto = verificationRequestService.reject(id, new RejectVerificationRequestCommand(
                payload.verifierId(),
                payload.reason()
        ));
        return ResponseEntity.accepted().body(map(dto));
    }

    private VerificationRequestResponse map(VerificationRequestDto dto) {
        return new VerificationRequestResponse(
                dto.id(),
                dto.ownerId(),
                dto.tripId(),
                dto.distanceKm(),
                dto.energyKwh(),
                dto.checksum(),
                dto.status(),
                dto.createdAt(),
                dto.verifiedAt(),
                dto.verifierId(),
                dto.notes()
        );
    }
}
