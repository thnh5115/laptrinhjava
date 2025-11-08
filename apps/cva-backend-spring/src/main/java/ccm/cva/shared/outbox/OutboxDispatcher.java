package ccm.cva.shared.outbox;

import ccm.cva.audit.client.AuditLogClient;
import ccm.cva.shared.trace.CorrelationIdHolder;
import ccm.cva.wallet.client.WalletClient;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final OutboxService outboxService;
    private final AuditLogClient auditLogClient;
    private final WalletClient walletClient;
    public OutboxDispatcher(
        OutboxService outboxService,
        AuditLogClient auditLogClient,
        WalletClient walletClient
    ) {
        this.outboxService = outboxService;
        this.auditLogClient = auditLogClient;
        this.walletClient = walletClient;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval:30s}")
    public void dispatch() {
        List<OutboxEvent> events = outboxService.fetchDispatchable();
        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            Optional<String> correlationId = Optional.ofNullable(event.getCorrelationId());
            correlationId.ifPresent(id -> {
                CorrelationIdHolder.set(id);
                MDC.put("correlationId", id);
                MDC.put("requestId", id);
            });

            try {
                switch (event.getType()) {
                    case AUDIT_EVENT -> handleAudit(event);
                    case WALLET_CREDIT -> handleWallet(event);
                    default -> {
                        log.warn("Unknown outbox event type {}", event.getType());
                        outboxService.markFailed(event, "Unsupported event type");
                    }
                }
            } finally {
                correlationId.ifPresent(id -> {
                    CorrelationIdHolder.clear();
                    MDC.remove("correlationId");
                    MDC.remove("requestId");
                });
            }
        }
    }

    private void handleAudit(OutboxEvent event) {
        outboxService.readAuditPayload(event).ifPresentOrElse(payload -> {
            try {
                auditLogClient.record(payload.action(), payload.payload());
                outboxService.markSucceeded(event);
            } catch (Exception ex) {
                log.warn("Retrying audit event {} failed: {}", event.getId(), ex.getMessage());
                outboxService.markFailed(event, ex.getMessage());
            }
        }, () -> log.error("Skipping audit event {} due to payload issues", event.getId()));
    }

    private void handleWallet(OutboxEvent event) {
        outboxService.readWalletPayload(event).ifPresentOrElse(payload -> {
            try {
                walletClient.credit(payload.ownerId(), payload.credits(), payload.correlationId(), payload.idempotencyKey());
                outboxService.markSucceeded(event);
            } catch (Exception ex) {
                log.warn("Retrying wallet credit {} failed: {}", event.getId(), ex.getMessage());
                outboxService.markFailed(event, ex.getMessage());
            }
        }, () -> log.error("Skipping wallet event {} due to payload issues", event.getId()));
    }
}
