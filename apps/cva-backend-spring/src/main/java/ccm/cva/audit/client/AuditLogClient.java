package ccm.cva.audit.client;

import java.util.Map;

public interface AuditLogClient {

    void record(String action, Map<String, Object> payload);
}
