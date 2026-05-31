package in.vis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
@ActiveProfiles("test")
class ActuatorPrometheusIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void prometheus_endpoint_exposes_every_vis_sm_c_meter() throws Exception {
        var result = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        org.hamcrest.Matchers.containsString("text/plain")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c1_ai_cost_wau")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c2_ws_disconnect")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c3_crash_free_rate")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c4_dpdpa_tickets")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c5_trainer_override")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c6_set_save_latency")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c7_marketplace_abuse")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c8_new_signup_no_log")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c9_batch_late_log")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vis_sm_c10_pr_card_no_share")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ai_cost_wau_spend")))
                .andReturn();
    }
}
