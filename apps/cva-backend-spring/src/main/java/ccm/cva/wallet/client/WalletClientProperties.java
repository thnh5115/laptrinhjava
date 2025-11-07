package ccm.cva.wallet.client;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.wallet")
@Validated
public class WalletClientProperties {

    /**
     * Base URL of the wallet service (e.g. https://wallet.internal or http://localhost:8084).
     * When not provided the HTTP wallet client remains disabled and the noop implementation is used.
     */
    private URI baseUrl;

    /**
     * Relative path of the credit endpoint. Defaults to /api/wallet/credits.
     */
    private String creditPath = "/api/wallet/credits";

    private Duration connectTimeout = Duration.ofSeconds(2);

    private Duration readTimeout = Duration.ofSeconds(5);

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCreditPath() {
        return creditPath;
    }

    public void setCreditPath(String creditPath) {
        this.creditPath = creditPath;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isEnabled() {
        return baseUrl != null;
    }
}
