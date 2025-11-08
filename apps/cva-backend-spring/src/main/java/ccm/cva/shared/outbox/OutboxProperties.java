package ccm.cva.shared.outbox;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.outbox")
public class OutboxProperties {

    private int maxAttempts = 5;
    private Duration initialBackoff = Duration.ofSeconds(1);
    private Duration maxBackoff = Duration.ofSeconds(30);
    private int pollBatchSize = 25;
    private Duration pollInterval = Duration.ofSeconds(30);

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getInitialBackoff() {
        return initialBackoff;
    }

    public void setInitialBackoff(Duration initialBackoff) {
        this.initialBackoff = initialBackoff;
    }

    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public int getPollBatchSize() {
        return pollBatchSize;
    }

    public void setPollBatchSize(int pollBatchSize) {
        this.pollBatchSize = pollBatchSize;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }
}
