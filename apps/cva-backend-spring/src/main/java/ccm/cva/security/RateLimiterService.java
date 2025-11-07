package ccm.cva.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * A very small in-memory rate limiter based on a fixed window counter. It is
 * sufficient for local development and demo purposes.
 */
@Component
public class RateLimiterService {

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int limit, Duration window) {
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());
        return counter.tryConsume(limit, window);
    }

    private static final class WindowCounter {

        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        synchronized boolean tryConsume(int limit, Duration window) {
            long now = System.currentTimeMillis();
            long windowMillis = window.toMillis();
            if (windowMillis <= 0) {
                windowMillis = 60_000L;
            }

            if (now - windowStart >= windowMillis) {
                windowStart = now;
                count = 0;
            }

            if (count >= limit) {
                return false;
            }

            count++;
            return true;
        }
    }
}
