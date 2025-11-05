package ccm.cva.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private RateRule approve = RateRule.of(10, Duration.ofMinutes(1));
    private RateRule reject = RateRule.of(10, Duration.ofMinutes(1));
    private RateRule report = RateRule.of(6, Duration.ofMinutes(1));
    private RateRule defaultRule = RateRule.of(30, Duration.ofMinutes(1));

    public RateRule approve() {
        return approve;
    }

    public void setApprove(RateRule approve) {
        this.approve = approve;
    }

    public RateRule reject() {
        return reject;
    }

    public void setReject(RateRule reject) {
        this.reject = reject;
    }

    public RateRule report() {
        return report;
    }

    public void setReport(RateRule report) {
        this.report = report;
    }

    public RateRule defaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(RateRule defaultRule) {
        this.defaultRule = defaultRule;
    }

    public RateRule resolve(String name) {
        return switch (name) {
            case "approve" -> approve;
            case "reject" -> reject;
            case "report" -> report;
            default -> defaultRule;
        };
    }

    public static class RateRule {
        private int limit;
        private Duration window;

        public static RateRule of(int limit, Duration window) {
            RateRule rule = new RateRule();
            rule.limit = limit;
            rule.window = window;
            return rule;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }
    }
}
