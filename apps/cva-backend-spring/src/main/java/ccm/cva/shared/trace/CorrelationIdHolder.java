package ccm.cva.shared.trace;

import java.util.Optional;

/**
 * Maintains the current correlation id for the active thread. This allows us to
 * propagate the identifier to downstream clients (audit, wallet) and to include
 * it in structured logs.
 */
public final class CorrelationIdHolder {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CorrelationIdHolder() {
    }

    public static void set(String correlationId) {
        CURRENT.set(correlationId);
    }

    public static Optional<String> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void clear() {
        CURRENT.remove();
    }
}
