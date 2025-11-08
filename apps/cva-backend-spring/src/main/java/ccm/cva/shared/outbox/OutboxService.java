package ccm.cva.shared.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxEventRepository repository;
    private final OutboxProperties properties;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository, OutboxProperties properties, ObjectMapper objectMapper) {
        this.repository = repository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void enqueueAuditEvent(AuditOutboxPayload payload, String correlationId) {
        persistEvent(OutboxEventType.AUDIT_EVENT, payload, correlationId, null);
    }

    @Transactional
    public void enqueueWalletCredit(WalletCreditOutboxPayload payload, String correlationId, String idempotencyKey) {
        persistEvent(OutboxEventType.WALLET_CREDIT, payload, correlationId, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> fetchDispatchable() {
        List<OutboxEvent> events = repository.findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            Collections.singletonList(OutboxStatus.PENDING),
            Instant.now(),
            PageRequest.of(0, properties.getPollBatchSize())
        );
        if (!events.isEmpty() && log.isDebugEnabled()) {
            log.debug("Outbox fetched {} events for dispatch", events.size());
        }
        return events;
    }

    @Transactional
    public void markSucceeded(OutboxEvent event) {
        event.markSent();
        repository.save(event);
    }

    @Transactional
    public void markFailed(OutboxEvent event, String errorMessage) {
        int nextAttempt = event.getAttempts() + 1;
        if (nextAttempt >= properties.getMaxAttempts()) {
            event.markFailed(errorMessage);
        } else {
            Duration backoff = computeBackoff(nextAttempt);
            Instant retryAt = Instant.now().plus(backoff);
            event.incrementAttempts(errorMessage, retryAt);
        }
        repository.save(event);
    }

    public Optional<AuditOutboxPayload> readAuditPayload(OutboxEvent event) {
        return deserialize(event, AuditOutboxPayload.class);
    }

    public Optional<WalletCreditOutboxPayload> readWalletPayload(OutboxEvent event) {
        return deserialize(event, WalletCreditOutboxPayload.class);
    }

    private void persistEvent(OutboxEventType type, Object payload, String correlationId, String idempotencyKey) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setType(type);
            event.setStatus(OutboxStatus.PENDING);
            event.setAttempts(0);
            event.setCorrelationId(correlationId);
            event.setIdempotencyKey(idempotencyKey);
            event.setNextAttemptAt(Instant.now().plus(properties.getInitialBackoff()));
            event.setPayload(objectMapper.writeValueAsString(payload));
            repository.save(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }

    private Duration computeBackoff(int attempt) {
        long base = properties.getInitialBackoff().toMillis();
        long max = properties.getMaxBackoff().toMillis();
        if (base <= 0L) {
            base = 1000L;
        }
        double multiplier = Math.pow(2.0, Math.max(0, attempt - 1));
        long delay = (long) Math.min(Math.round(base * multiplier), max > 0 ? max : Long.MAX_VALUE);
        return Duration.ofMillis(Math.max(delay, base));
    }

    private <T> Optional<T> deserialize(OutboxEvent event, Class<T> target) {
        try {
            return Optional.of(objectMapper.readValue(event.getPayload(), target));
        } catch (JsonProcessingException ex) {
            log.error("Failed to deserialize outbox payload for event {}", event.getId(), ex);
            markFailed(event, "Payload deserialization error: " + ex.getMessage());
            return Optional.empty();
        }
    }
}
