package ccm.cva.wallet.client;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class WalletHttpClient implements WalletClient {

    private static final Logger log = LoggerFactory.getLogger(WalletHttpClient.class);

    private final RestTemplate restTemplate;
    private final WalletClientProperties properties;

    public WalletHttpClient(RestTemplate restTemplate, WalletClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public void credit(Long ownerId, BigDecimal credits, String correlationId, String idempotencyKey) { // SỬA: Long ownerId
        Assert.notNull(ownerId, "ownerId must not be null");
        Assert.notNull(credits, "credits must not be null");
        Assert.hasText(idempotencyKey, "idempotencyKey must not be blank");

        // Tạo request body với Long ownerId
        WalletCreditRequest body = new WalletCreditRequest(ownerId, credits, correlationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
        headers.add("X-Idempotency-Key", idempotencyKey);
        if (correlationId != null && !correlationId.isBlank()) {
            headers.add("X-Correlation-Id", correlationId);
        }

        HttpEntity<WalletCreditRequest> request = new HttpEntity<>(body, headers);

        URI target = resolveCreditUri();
        try {
            ResponseEntity<Void> response = restTemplate.exchange(target, HttpMethod.POST, request, Void.class);
            if (log.isDebugEnabled()) {
                log.debug("Wallet credited ownerId={} credits={} status={}", ownerId, credits, response.getStatusCode());
            }
        } catch (HttpStatusCodeException ex) {
            String message = "Wallet service rejected credit request with status %s: %s".formatted(
                ex.getStatusCode(), ex.getResponseBodyAsString()
            );
            throw new IllegalStateException(message, ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Failed to communicate with wallet service", ex);
        }
    }

    private URI resolveCreditUri() {
        URI base = Objects.requireNonNull(properties.getBaseUrl(), "Wallet base URL must be configured");
        String creditPath = properties.getCreditPath();
        if (creditPath == null || creditPath.isBlank()) {
            creditPath = "/api/owner/wallet/credits"; // Cập nhật đường dẫn mới cho đúng Owner Backend
        }
        return base.resolve(creditPath.startsWith("/") ? creditPath : "/" + creditPath);
    }

    // SỬA: Record này cũng phải dùng Long
    private record WalletCreditRequest(Long ownerId, BigDecimal amount, String correlationId) { }
}