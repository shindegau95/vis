package in.vis.filter;

import in.vis.config.RateLimitProperties;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private RateLimitProperties props;
    private ProxyManager<String> proxyManager;
    private RateLimitFilter filter;
    private FilterChain chain;
    private Map<String, BucketProxy> buckets;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        props = new RateLimitProperties(3, 3, Duration.ofMinutes(1));
        proxyManager = mock(ProxyManager.class);
        chain = mock(FilterChain.class);
        buckets = new HashMap<>();

        doAnswer(inv -> {
            String key = inv.getArgument(0);
            return buckets.computeIfAbsent(key, k -> mock(BucketProxy.class));
        }).when(proxyManager).getProxy(anyString(), any(Supplier.class));

        filter = new RateLimitFilter(proxyManager, props);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String uid) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private void stubBucket(String uid, ConsumptionProbe... probes) {
        BucketProxy b = buckets.computeIfAbsent(uid, k -> mock(BucketProxy.class));
        if (probes.length == 1) {
            doReturn(probes[0]).when(b).tryConsumeAndReturnRemaining(1L);
        } else {
            doReturn(probes[0], java.util.Arrays.copyOfRange(probes, 1, probes.length))
                    .when(b).tryConsumeAndReturnRemaining(1L);
        }
    }

    @Test
    void capacity_exhausts_then_returns_429_with_retry_after() throws Exception {
        authenticateAs("uid-a");
        stubBucket("uid-a",
                ConsumptionProbe.consumed(2L, 60_000_000_000L),
                ConsumptionProbe.consumed(1L, 60_000_000_000L),
                ConsumptionProbe.consumed(0L, 60_000_000_000L),
                ConsumptionProbe.rejected(0L, 30_000_000_000L, 60_000_000_000L));

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/me");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            assertThat(res.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(res.getHeader(RateLimitFilter.HEADER_REMAINING)).isNotNull();
        }

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/me");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(res.getHeader(HttpHeaders.RETRY_AFTER)).isNotNull();
        assertThat(Integer.parseInt(res.getHeader(HttpHeaders.RETRY_AFTER))).isGreaterThanOrEqualTo(1);
        assertThat(res.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(res.getContentAsString()).contains("\"status\":429").contains("Too Many Requests");
        verify(chain, times(3)).doFilter(any(), any());
    }

    @Test
    void refill_window_resets_bucket() throws Exception {
        authenticateAs("uid-r");
        stubBucket("uid-r",
                ConsumptionProbe.rejected(0L, 1_000_000_000L, 60_000_000_000L),
                ConsumptionProbe.consumed(2L, 60_000_000_000L));

        MockHttpServletRequest first = new MockHttpServletRequest("GET", "/api/x");
        MockHttpServletResponse firstRes = new MockHttpServletResponse();
        filter.doFilter(first, firstRes, chain);
        assertThat(firstRes.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        MockHttpServletRequest second = new MockHttpServletRequest("GET", "/api/x");
        MockHttpServletResponse secondRes = new MockHttpServletResponse();
        filter.doFilter(second, secondRes, chain);
        assertThat(secondRes.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(secondRes.getHeader(RateLimitFilter.HEADER_REMAINING)).isEqualTo("2");
    }

    @Test
    void distinct_uids_have_independent_buckets() throws Exception {
        stubBucket("uid-a", ConsumptionProbe.rejected(0L, 60_000_000_000L, 60_000_000_000L));
        stubBucket("uid-b", ConsumptionProbe.consumed(2L, 60_000_000_000L));

        authenticateAs("uid-a");
        MockHttpServletResponse resA = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/m"), resA, chain);
        assertThat(resA.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        SecurityContextHolder.clearContext();
        authenticateAs("uid-b");
        MockHttpServletResponse resB = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/m"), resB, chain);
        assertThat(resB.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void actuator_paths_bypass_filter() throws Exception {
        authenticateAs("uid-z");

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
        req.setServletPath("/actuator/health");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        verify(proxyManager, never()).getProxy(anyString(), any(Supplier.class));
    }

    @Test
    void anonymous_authentication_is_not_rate_limited() throws Exception {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        Authentication anon = new AnonymousAuthenticationToken(
                "anon-key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        ctx.setAuthentication(anon);
        SecurityContextHolder.setContext(ctx);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/anything");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(chain).doFilter(req, res);
        verify(proxyManager, never()).getProxy(eq("anonymousUser"), any(Supplier.class));
    }

    @Test
    void null_authentication_is_not_rate_limited() throws Exception {
        SecurityContextHolder.clearContext();

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/anything");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(chain).doFilter(req, res);
        verify(proxyManager, never()).getProxy(anyString(), any(Supplier.class));
    }

    @Test
    void properties_validation_rejects_invalid_inputs() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RateLimitProperties(0, 1, Duration.ofMinutes(1)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RateLimitProperties(1, 0, Duration.ofMinutes(1)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RateLimitProperties(1, 1, Duration.ZERO));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RateLimitProperties(1, 1, null));
    }
}
