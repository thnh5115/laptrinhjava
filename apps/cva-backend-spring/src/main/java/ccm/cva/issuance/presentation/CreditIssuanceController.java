package ccm.cva.issuance.presentation;

import ccm.common.dto.paging.PageResponse;
import ccm.cva.issuance.application.query.CreditIssuanceQuery;
import ccm.cva.issuance.application.service.IssuanceService;
import ccm.cva.issuance.presentation.dto.CreditIssuanceHistoryResponse;
import ccm.cva.issuance.presentation.mapper.CreditIssuanceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cva/issuances")
@Tag(name = "CVA Issuances", description = "Issued carbon credit history")
public class CreditIssuanceController {

    private final IssuanceService issuanceService;
    private final CreditIssuanceMapper mapper;

    public CreditIssuanceController(IssuanceService issuanceService, CreditIssuanceMapper mapper) {
        this.issuanceService = issuanceService;
        this.mapper = mapper;
    }

    @Operation(summary = "List issued carbon credits")
    @GetMapping
    public ResponseEntity<PageResponse<CreditIssuanceHistoryResponse>> history(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) String search
    ) {
        CreditIssuanceQuery query = new CreditIssuanceQuery(ownerId, createdFrom, createdTo, search);
        Page<ccm.cva.issuance.domain.CreditIssuance> page = issuanceService.search(query, pageable);
        List<CreditIssuanceHistoryResponse> content = page.getContent().stream()
            .map(mapper::toHistoryResponse)
            .toList();

        PageResponse<CreditIssuanceHistoryResponse> response = new PageResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            pageable.getSort().toString()
        );
        return ResponseEntity.ok(response);
    }
}
