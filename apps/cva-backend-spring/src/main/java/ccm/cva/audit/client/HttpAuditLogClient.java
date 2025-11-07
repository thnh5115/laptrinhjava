package ccm.cva.audit.client;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Sends audit events to the Admin service over HTTP. The admin service is responsible for
 * persisting and displaying audit trails across the platform.
 */
public class HttpAuditLogClient implements AuditLogClient {

    private static final Logger log = LoggerFactory.getLogger(HttpAuditLogClient.class);

    private final RestTemplate restTemplate;
    private final AuditClientProperties properties;

    public HttpAuditLogClient(RestTemplate restTemplate, AuditClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public void record(String action, Map<String, Object> payload) {
        Objects.requireNonNull(action, "action must not be null");
        AuditEventRequest event = new AuditEventRequest(action, Instant.now(), payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuditEventRequest> entity = new HttpEntity<>(event, headers);

        URI recordUri = resolveRecordUri();
        try {
            ResponseEntity<Void> response = restTemplate.exchange(recordUri, HttpMethod.POST, entity, Void.class);
            if (log.isDebugEnabled()) {
                log.debug("Audit event {} recorded with status {}", action, response.getStatusCode());
            }
        } catch (HttpStatusCodeException ex) {
            String message = "Audit service rejected event with status %s: %s".formatted(
                ex.getStatusCode(), ex.getResponseBodyAsString()
            );
            throw new IllegalStateException(message, ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Failed to send audit event", ex);
        }
    }

    private URI resolveRecordUri() {
        URI base = Objects.requireNonNull(properties.getBaseUrl(), "Audit base URL must be configured");
        String recordPath = properties.getRecordPath();
        if (recordPath == null || recordPath.isBlank()) {
            recordPath = "/api/admin/audit/logs";
        }
        return base.resolve(recordPath.startsWith("/") ? recordPath : "/" + recordPath);
    }

    private record AuditEventRequest(String action, Instant occurredAt, Map<String, Object> payload) {}
}
