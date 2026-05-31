package in.vis.observability;

public final class VisMetrics {

    public static final String SM_C1_AI_COST_WAU = "vis.sm.c1.ai_cost_wau";
    public static final String SM_C2_WS_DISCONNECT = "vis.sm.c2.ws_disconnect";
    public static final String SM_C3_CRASH_FREE = "vis.sm.c3.crash_free_rate";
    public static final String SM_C4_DPDPA_TICKETS = "vis.sm.c4.dpdpa_tickets";
    public static final String SM_C5_TRAINER_OVERRIDE = "vis.sm.c5.trainer_override";

    /**
     * SM-C6 is a Timer, not a Counter — the PRD threshold is p95 latency > 1.5s, which
     * requires quantile aggregation. Call sites must use {@code meterRegistry.timer(...).record(elapsed)},
     * never {@code meterRegistry.counter(...)}.
     */
    public static final String SM_C6_SET_SAVE_LATENCY = "vis.sm.c6.set_save_latency";

    public static final String SM_C7_MARKETPLACE_ABUSE = "vis.sm.c7.marketplace_abuse";
    public static final String SM_C8_NEW_SIGNUP_NO_LOG = "vis.sm.c8.new_signup_no_log";
    public static final String SM_C9_BATCH_LATE_LOG = "vis.sm.c9.batch_late_log";
    public static final String SM_C10_PR_CARD_NO_SHARE = "vis.sm.c10.pr_card_no_share";

    public static final String AI_COST_WAU_SPEND = "ai.cost.wau_spend";

    public static final String TAG_ENV = "env";
    public static final String TAG_APPLICATION = "application";
    public static final String TAG_TRAINER_ID = "trainer_id";
    public static final String TAG_SOURCE = "source";

    public static final String APPLICATION_NAME = "vis-backend";

    private VisMetrics() {}
}
