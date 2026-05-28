package in.vis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = VisApplication.class)
@ActiveProfiles("test")
@Testcontainers
class RedisIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.host", REDIS::getHost);
        r.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    HealthEndpoint healthEndpoint;

    @Test
    void round_trip_set_get() {
        redis.opsForValue().set("vis:test:key", "pong");
        assertThat(redis.opsForValue().get("vis:test:key")).isEqualTo("pong");
    }

    @Test
    void actuator_reports_redis_up() {
        var health = healthEndpoint.healthForPath("redis");
        assertThat(health)
                .as("`redis` component must be present in /actuator/health components map")
                .isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }
}
