package ccm.cva.wallet.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WalletHttpClientTest {

    private WalletClientProperties properties;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private WalletHttpClient client;

    @BeforeEach
    void setUp() {
        properties = new WalletClientProperties();
        properties.setBaseUrl(URI.create("http://wallet.test"));
        properties.setCreditPath("/api/wallet/credits");

        restTemplate = new RestTemplateBuilder().build();
        restTemplate.setRequestFactory(new NoTimeoutRequestFactory());
        server = MockRestServiceServer.createServer(restTemplate);

        client = new WalletHttpClient(restTemplate, properties);
    }

    @Test
    void creditSendsHeadersAndPayload() {
        UUID ownerId = UUID.randomUUID();
        server.expect(ExpectedCount.once(), requestTo("http://wallet.test/api/wallet/credits"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("X-Idempotency-Key", "idem-1"))
            .andExpect(header("X-Correlation-Id", "corr-1"))
            .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
            .andExpect(jsonPath("$.amount").value("13.32"))
            .andRespond(withStatus(HttpStatus.ACCEPTED));

        client.credit(ownerId, new BigDecimal("13.32"), "corr-1", "idem-1");
        server.verify();
    }

    @Test
    void creditWrapsErrorFromService() {
        UUID ownerId = UUID.randomUUID();
        server.expect(ExpectedCount.once(), requestTo("http://wallet.test/api/wallet/credits"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("duplicate"));

        assertThatThrownBy(() -> client.credit(ownerId, BigDecimal.ONE, null, "idem-2"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Wallet service rejected");
    }

    /**
     * Request factory that disables timeouts for deterministic tests.
     */
    private static final class NoTimeoutRequestFactory extends org.springframework.http.client.SimpleClientHttpRequestFactory {
        @Override
        public void setConnectTimeout(int connectTimeout) {
            super.setConnectTimeout(0);
        }

        @Override
        public void setReadTimeout(int readTimeout) {
            super.setReadTimeout(0);
        }
    }
}
