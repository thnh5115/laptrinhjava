package ccm.cva.shared.outbox;

import java.util.Map;

public record AuditOutboxPayload(String action, Map<String, Object> payload) { }
