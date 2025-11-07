package ccm.cva.audit.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class HttpAuditLogClientTest {

    private AuditClientProperties properties;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private HttpAuditLogClient client;

    @BeforeEach
    void setUp() {
        properties = new AuditClientProperties();
        properties.setBaseUrl(URI.create("http://admin.test"));
        properties.setRecordPath("/api/audit/events");

        restTemplate = new RestTemplateBuilder().build();
        restTemplate.setRequestFactory(new NoTimeoutRequestFactory());
        server = MockRestServiceServer.createServer(restTemplate);

        client = new HttpAuditLogClient(restTemplate, properties);
    }

    @Test
    void recordPostsEventPayload() {
        server.expect(ExpectedCount.once(), requestTo("http://admin.test/api/audit/events"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.action").value("cva.request.approved"))
            .andExpect(jsonPath("$.payload.requestId").value("abc"))
            .andRespond(withStatus(HttpStatus.ACCEPTED));

        client.record("cva.request.approved", Map.of("requestId", "abc"));
        server.verify();
    }

    @Test
    void recordWrapsError() {
        server.expect(ExpectedCount.once(), requestTo("http://admin.test/api/audit/events"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("down"));

        assertThatThrownBy(() -> client.record("action", Map.of()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Audit service rejected");
    }

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
