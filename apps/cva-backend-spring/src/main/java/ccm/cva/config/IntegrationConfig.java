package ccm.cva.config;

import ccm.cva.audit.client.AuditClientProperties;
import ccm.cva.audit.client.AuditLogClient;
import ccm.cva.audit.client.HttpAuditLogClient;
import ccm.cva.shared.trace.CorrelationIdFilter;
import ccm.cva.shared.trace.CorrelationIdHolder;
import ccm.cva.wallet.client.WalletClient;
import ccm.cva.wallet.client.WalletClientProperties;
import ccm.cva.wallet.client.WalletHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({WalletClientProperties.class, AuditClientProperties.class})
public class IntegrationConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.wallet", name = "base-url")
    public WalletClient walletHttpClient(RestTemplateBuilder builder, WalletClientProperties properties) {
        RestTemplate restTemplate = builder
            .requestFactory(SimpleClientHttpRequestFactory::new)
            .additionalInterceptors((request, body, execution) -> {
                CorrelationIdHolder.get().ifPresent(id -> request.getHeaders().add(CorrelationIdFilter.HEADER_NAME, id));
                return execution.execute(request, body);
            })
            .build();
        restTemplate.setRequestFactory(requestFactory(properties.getConnectTimeout(), properties.getReadTimeout()));
        return new WalletHttpClient(restTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean(WalletClient.class)
    public WalletClient noopWalletClient() {
        return new ccm.cva.wallet.client.NoopWalletClient();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.audit", name = "base-url")
    public AuditLogClient httpAuditLogClient(RestTemplateBuilder builder, AuditClientProperties properties) {
        RestTemplate restTemplate = builder
            .requestFactory(SimpleClientHttpRequestFactory::new)
            .additionalInterceptors((request, body, execution) -> {
                CorrelationIdHolder.get().ifPresent(id -> request.getHeaders().add(CorrelationIdFilter.HEADER_NAME, id));
                return execution.execute(request, body);
            })
            .build();
        restTemplate.setRequestFactory(requestFactory(properties.getConnectTimeout(), properties.getReadTimeout()));
        return new HttpAuditLogClient(restTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean(AuditLogClient.class)
    public AuditLogClient loggingAuditLogClient() {
        return new ccm.cva.audit.client.LoggingAuditLogClient();
    }

    private ClientHttpRequestFactory requestFactory(java.time.Duration connectTimeout, java.time.Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        if (connectTimeout != null) {
            factory.setConnectTimeout((int) connectTimeout.toMillis());
        }
        if (readTimeout != null) {
            factory.setReadTimeout((int) readTimeout.toMillis());
        }
        return factory;
    }

}
