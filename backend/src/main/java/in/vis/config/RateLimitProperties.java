package in.vis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
        int capacity,
        int refillTokens,
        Duration refillPeriod
) {
    public RateLimitProperties {
        if (capacity <= 0) {
            throw new IllegalArgumentException("app.rate-limit.capacity must be > 0");
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException("app.rate-limit.refill-tokens must be > 0");
        }
        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("app.rate-limit.refill-period must be a positive ISO-8601 duration");
        }
    }
}
