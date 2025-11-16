package ccm.cva.audit.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class LoggingAuditLogClient implements AuditLogClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuditLogClient.class);

    @Override
    public void record(String action, Map<String, Object> payload) {
        log.info("[AUDIT] action={} payload={} ", action, payload);
    }
}
