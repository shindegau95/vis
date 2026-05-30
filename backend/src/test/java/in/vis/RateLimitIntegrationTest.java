package in.vis;

import in.vis.config.RateLimitProperties;
import in.vis.filter.RateLimitFilter;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RateLimitIntegrationTest {

    private static final int CAPACITY = 3;
    private static final Duration REFILL = Duration.ofMinutes(1);

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    private static RedisClient redisClient;
    private static StatefulRedisConnection<String, byte[]> connection;
    private static ProxyManager<String> proxyManager;
    private static RateLimitFilter filter;

    @BeforeAll
    static void wire() {
        RedisURI uri = RedisURI.builder()
                .withHost(REDIS.getHost())
                .withPort(REDIS.getFirstMappedPort())
                .build();
        redisClient = RedisClient.create(uri);
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        connection = redisClient.connect(codec);

        proxyManager = Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(REFILL.multipliedBy(2)))
                .build();

        RateLimitProperties props = new RateLimitProperties(CAPACITY, CAPACITY, REFILL);
        filter = new RateLimitFilter(proxyManager, props);
    }

    @AfterAll
    static void shutdown() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String uid) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList()));
        SecurityContextHolder.setContext(ctx);
    }

    private MockHttpServletResponse runOnce(String uri) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setServletPath(uri);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();
        filter.doFilter(req, res, chain);
        return res;
    }

    @Test
    void capacity_plus_one_returns_429_with_retry_after_against_real_redis() throws Exception {
        String uid = "uid-" + UUID.randomUUID();
        authenticateAs(uid);

        for (int i = 0; i < CAPACITY; i++) {
            MockHttpServletResponse res = runOnce("/api/me");
            assertThat(res.getStatus()).as("request %d should succeed", i + 1).isEqualTo(HttpStatus.OK.value());
        }

        MockHttpServletResponse over = runOnce("/api/me");
        assertThat(over.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(over.getHeader(HttpHeaders.RETRY_AFTER)).isNotNull();
        assertThat(Integer.parseInt(over.getHeader(HttpHeaders.RETRY_AFTER))).isGreaterThanOrEqualTo(1);
        assertThat(over.getContentAsString()).contains("\"status\":429");
    }

    @Test
    void distinct_uids_have_independent_buckets_against_real_redis() throws Exception {
        String uidA = "uid-a-" + UUID.randomUUID();
        String uidB = "uid-b-" + UUID.randomUUID();

        authenticateAs(uidA);
        for (int i = 0; i < CAPACITY; i++) {
            runOnce("/api/x");
        }
        MockHttpServletResponse aBlocked = runOnce("/api/x");
        assertThat(aBlocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        SecurityContextHolder.clearContext();
        authenticateAs(uidB);
        MockHttpServletResponse bFirst = runOnce("/api/x");
        assertThat(bFirst.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void actuator_health_bypasses_against_real_redis() throws Exception {
        String uid = "uid-actuator-" + UUID.randomUUID();
        authenticateAs(uid);

        for (int i = 0; i < CAPACITY * 5; i++) {
            MockHttpServletResponse res = runOnce("/actuator/health");
            assertThat(res.getStatus())
                    .as("actuator request %d must never 429", i + 1)
                    .isEqualTo(HttpStatus.OK.value());
        }
    }
}
