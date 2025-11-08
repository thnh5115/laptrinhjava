package ccm.cva.config;

import ccm.cva.shared.outbox.OutboxProperties;
import java.util.Collections;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate externalRetryTemplate(OutboxProperties properties) {
        RetryTemplate template = new RetryTemplate();

        long initialInterval = Math.max(100L, properties.getInitialBackoff().toMillis());
        long maxInterval = Math.max(initialInterval, properties.getMaxBackoff().toMillis());

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(2.0);
        template.setBackOffPolicy(backOffPolicy);

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = Collections.singletonMap(Exception.class, true);
    int immediateAttempts = Math.max(1, Math.min(3, properties.getMaxAttempts()));
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(immediateAttempts, retryableExceptions);
        template.setRetryPolicy(retryPolicy);

        return template;
    }
}
