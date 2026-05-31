package in.vis.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class MetricsRegistrar {

    private final MeterRegistry meterRegistry;
    private final String env;

    private final AtomicReference<Double> crashFreeRate = new AtomicReference<>(1.0);
    private final AtomicReference<Double> aiCostWauSpend = new AtomicReference<>(0.0);

    public MetricsRegistrar(MeterRegistry meterRegistry,
                            @Value("${app.observability.env:dev}") String env) {
        this.meterRegistry = meterRegistry;
        this.env = env;
    }

    @PostConstruct
    void registerAllMeters() {
        Tags base = Tags.of(VisMetrics.TAG_ENV, env, VisMetrics.TAG_APPLICATION, VisMetrics.APPLICATION_NAME);

        Counter.builder(VisMetrics.SM_C1_AI_COST_WAU).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C2_WS_DISCONNECT).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C4_DPDPA_TICKETS).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C5_TRAINER_OVERRIDE).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C7_MARKETPLACE_ABUSE).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C8_NEW_SIGNUP_NO_LOG).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C9_BATCH_LATE_LOG).tags(base).register(meterRegistry);
        Counter.builder(VisMetrics.SM_C10_PR_CARD_NO_SHARE).tags(base).register(meterRegistry);

        Timer.builder(VisMetrics.SM_C6_SET_SAVE_LATENCY)
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(base)
                .register(meterRegistry);

        Gauge.builder(VisMetrics.SM_C3_CRASH_FREE, crashFreeRate, AtomicReference::get)
                .strongReference(true)
                .tags(base.and(VisMetrics.TAG_SOURCE, "backend-placeholder"))
                .register(meterRegistry);

        Gauge.builder(VisMetrics.AI_COST_WAU_SPEND, aiCostWauSpend, AtomicReference::get)
                .strongReference(true)
                .tags(base.and(VisMetrics.TAG_TRAINER_ID, "__unbootstrapped__"))
                .register(meterRegistry);
    }
}
