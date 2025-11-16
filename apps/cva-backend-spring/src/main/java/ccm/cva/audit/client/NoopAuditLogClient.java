package ccm.cva.audit.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class NoopAuditLogClient implements AuditLogClient {

    private static final Logger log = LoggerFactory.getLogger(NoopAuditLogClient.class);

    @Override
    public void record(String action, Map<String, Object> payload) {
        log.debug("[NOOP] audit action={} payload={}", action, payload);
    }
}
