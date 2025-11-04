package ccm.cva.report.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cva/reports")
@Tag(name = "CVA Reports", description = "Carbon audit report generation")
public class ReportController {

    @Operation(summary = "Generate carbon audit report (placeholder)")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable UUID id) {
        // Week 1 stub: respond with placeholder payload
        Map<String, Object> payload = Map.of(
            "requestId", id,
            "status", "PENDING",
            "message", "Report generation will be available in week 3 milestone"
        );
        return ResponseEntity.accepted().body(payload);
    }
}
