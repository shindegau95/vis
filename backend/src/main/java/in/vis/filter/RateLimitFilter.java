package in.vis.filter;

import in.vis.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RateLimitFilter extends OncePerRequestFilter {

    static final String HEADER_REMAINING = "X-Rate-Limit-Remaining";

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties props;

    public RateLimitFilter(ProxyManager<String> proxyManager, RateLimitProperties props) {
        this.proxyManager = proxyManager;
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path != null && path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth.getName() == null
                || auth.getName().isBlank()
                || "anonymousUser".equals(auth.getName())) {
            chain.doFilter(req, res);
            return;
        }

        String uid = auth.getName();
        Bucket bucket = proxyManager.getProxy(uid, this::bucketConfig);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            res.setHeader(HEADER_REMAINING, Long.toString(probe.getRemainingTokens()));
            chain.doFilter(req, res);
            return;
        }

        long retryAfterSeconds = Math.max(
                1L,
                TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);

        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,"
                        + "\"detail\":\"Rate limit exceeded. Retry after " + retryAfterSeconds + "s.\"}"
        );
    }

    private BucketConfiguration bucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(props.capacity())
                        .refillGreedy(props.refillTokens(), props.refillPeriod()))
                .build();
    }
}
