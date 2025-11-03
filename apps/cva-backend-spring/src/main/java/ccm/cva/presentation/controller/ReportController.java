package ccm.cva.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cva/reports")
@Tag(name = "Reports", description = "Endpoints for carbon audit reports")
public class ReportController {

    @Operation(summary = "Generate verification report (stub)")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getReport(
            @PathVariable UUID id,
            @RequestParam(name = "format", defaultValue = "json") String format) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "requestId", id.toString(),
                        "format", format,
                        "status", "Report generation scheduled"
                ));
    }
}
