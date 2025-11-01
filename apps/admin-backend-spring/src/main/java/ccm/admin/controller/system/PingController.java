package ccm.admin.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Ping Controller - Public Health Check Endpoint
 * 
 * Provides a simple public endpoint for frontend to verify backend connectivity
 * before authentication. Useful for:
 * - Pre-login health checks
 * - Load balancer health probes
 * - Monitoring service uptime
 * - Network connectivity verification
 * 
 * PR-7: BLOCKER #4 - Missing public /api/ping endpoint
 */
@RestController
@RequestMapping("/api")
@Tag(name = "System", description = "System health and status endpoints")
public class PingController {

    /**
     * Public ping endpoint
     * 
     * Returns a simple OK status with timestamp to verify the backend is reachable.
     * Does not require authentication.
     * 
     * @return 200 OK with status and timestamp
     */
    @GetMapping("/ping")
    @Operation(
            summary = "Ping endpoint",
            description = "Public endpoint to verify backend connectivity. Does not require authentication."
    )
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "timestamp", Instant.now().toString(),
                "service", "Admin Backend API",
                "version", "1.0.0"
        ));
    }
}
