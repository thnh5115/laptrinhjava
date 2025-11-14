package ccm.cva.journey.api;

import ccm.admin.journey.entity.Journey;
import ccm.common.dto.paging.PageResponse;
import ccm.cva.journey.dto.ApproveJourneyPayload;
import ccm.cva.journey.dto.JourneyDecisionStatus;
import ccm.cva.journey.dto.JourneyResponse;
import ccm.cva.journey.dto.RejectJourneyPayload;
import ccm.cva.journey.service.JourneyApprovalCommand;
import ccm.cva.journey.service.JourneyQuery;
import ccm.cva.journey.service.JourneyRejectionCommand;
import ccm.cva.journey.service.JourneyReviewService;
import ccm.cva.journey.support.JourneyMapper;
import ccm.cva.security.RateLimited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cva/requests")
@Tag(name = "CVA Journeys", description = "Review and disposition of pending journeys")
public class JourneyReviewController {

    private final JourneyReviewService service;

    public JourneyReviewController(JourneyReviewService service) {
        this.service = service;
    }

    @Operation(summary = "List journeys pending review")
    @GetMapping
    public PageResponse<JourneyResponse> list(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) JourneyDecisionStatus status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String search
    ) {
        JourneyQuery query = new JourneyQuery(status, ownerId, createdFrom, createdTo, search);
        Page<Journey> page = service.search(query, pageable);
        List<JourneyResponse> content = page.getContent().stream().map(JourneyMapper::toResponse).toList();
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

    @Operation(summary = "Get journey details")
    @GetMapping("/{id}")
    public JourneyResponse detail(@PathVariable Long id) {
        return JourneyMapper.toResponse(service.get(id));
    }

    @Operation(summary = "Approve a journey")
    @PutMapping("/{id}/approve")
    @RateLimited("approve")
    public ResponseEntity<JourneyResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveJourneyPayload payload
    ) {
        Journey journey = service.approve(id, new JourneyApprovalCommand(payload.verifierId(), payload.overrideCredits()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(JourneyMapper.toResponse(journey));
    }

    @Operation(summary = "Reject a journey")
    @PutMapping("/{id}/reject")
    @RateLimited("reject")
    public ResponseEntity<JourneyResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectJourneyPayload payload
    ) {
        Journey journey = service.reject(id, new JourneyRejectionCommand(payload.verifierId(), payload.reason()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(JourneyMapper.toResponse(journey));
    }
}
