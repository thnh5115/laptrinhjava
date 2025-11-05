package ccm.cva.config;

import ccm.cva.audit.client.AuditClientProperties;
import ccm.cva.audit.client.AuditLogClient;
import ccm.cva.audit.client.HttpAuditLogClient;
import ccm.cva.wallet.client.WalletClient;
import ccm.cva.wallet.client.WalletClientProperties;
import ccm.cva.wallet.client.WalletHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({WalletClientProperties.class, AuditClientProperties.class})
public class IntegrationConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.wallet", name = "base-url")
    @Primary
    public WalletClient walletHttpClient(RestTemplateBuilder builder, WalletClientProperties properties) {
        RestTemplate restTemplate = builder
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
    @Primary
    public AuditLogClient httpAuditLogClient(RestTemplateBuilder builder, AuditClientProperties properties) {
        RestTemplate restTemplate = builder
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
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        if (connectTimeout != null) {
            int millis = (int) connectTimeout.toMillis();
            factory.setConnectTimeout(millis);
            factory.setConnectionRequestTimeout(millis);
        }
        if (readTimeout != null) {
            factory.setReadTimeout((int) readTimeout.toMillis());
        }
        return factory;
    }
}
