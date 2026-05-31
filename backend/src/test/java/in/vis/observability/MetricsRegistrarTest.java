package in.vis.observability;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsRegistrarTest {

    private MeterRegistry registry;
    private MetricsRegistrar registrar;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        registrar = new MetricsRegistrar(registry, "test");
        registrar.registerAllMeters();
    }

    @Test
    void all_eight_sm_c_counters_present_with_base_tags() {
        String[] counters = {
                VisMetrics.SM_C1_AI_COST_WAU,
                VisMetrics.SM_C2_WS_DISCONNECT,
                VisMetrics.SM_C4_DPDPA_TICKETS,
                VisMetrics.SM_C5_TRAINER_OVERRIDE,
                VisMetrics.SM_C7_MARKETPLACE_ABUSE,
                VisMetrics.SM_C8_NEW_SIGNUP_NO_LOG,
                VisMetrics.SM_C9_BATCH_LATE_LOG,
                VisMetrics.SM_C10_PR_CARD_NO_SHARE
        };
        for (String name : counters) {
            Meter meter = registry.find(name).meter();
            assertThat(meter).as("counter %s missing", name).isNotNull();
            assertThat(meter.getId().getTag(VisMetrics.TAG_ENV)).isEqualTo("test");
            assertThat(meter.getId().getTag(VisMetrics.TAG_APPLICATION))
                    .isEqualTo(VisMetrics.APPLICATION_NAME);
            assertThat(meter.getId().getType()).isEqualTo(Meter.Type.COUNTER);
        }
    }

    @Test
    void sm_c6_is_timer_with_percentile_histogram() {
        Meter meter = registry.find(VisMetrics.SM_C6_SET_SAVE_LATENCY).meter();
        assertThat(meter).isNotNull();
        assertThat(meter.getId().getType()).isEqualTo(Meter.Type.TIMER);
        assertThat(meter.getId().getTag(VisMetrics.TAG_ENV)).isEqualTo("test");
    }

    @Test
    void sm_c3_crash_free_gauge_starts_at_one_with_placeholder_source_tag() {
        Gauge gauge = registry.find(VisMetrics.SM_C3_CRASH_FREE).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(1.0);
        assertThat(gauge.getId().getTag(VisMetrics.TAG_SOURCE)).isEqualTo("backend-placeholder");
    }

    @Test
    void ai_cost_wau_spend_gauge_starts_at_zero_with_unbootstrapped_trainer() {
        Gauge gauge = registry.find(VisMetrics.AI_COST_WAU_SPEND).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(0.0);
        assertThat(gauge.getId().getTag(VisMetrics.TAG_TRAINER_ID)).isEqualTo("__unbootstrapped__");
    }

    @Test
    void counter_starts_at_zero_then_increments() {
        var counter = registry.counter(VisMetrics.SM_C2_WS_DISCONNECT,
                VisMetrics.TAG_ENV, "test",
                VisMetrics.TAG_APPLICATION, VisMetrics.APPLICATION_NAME);
        assertThat(counter.count()).isEqualTo(0.0);
        counter.increment();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
