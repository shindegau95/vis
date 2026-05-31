package in.vis;

import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.tracing.sampling.probability=0.0")
@AutoConfigureMockMvc
@AutoConfigureObservability
@ActiveProfiles("test")
@Import(TracingZeroSamplingIntegrationTest.TestTracingConfig.class)
class TracingZeroSamplingIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired InMemorySpanExporter inMemorySpanExporter;

    @BeforeEach
    void resetSpans() {
        inMemorySpanExporter.reset();
    }

    @Test
    void zero_sampling_exports_no_spans() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("uid-no-sample", null, Collections.emptyList());

        mockMvc.perform(get("/branches").with(authentication(auth)))
                .andExpect(status().isOk());

        assertThat(inMemorySpanExporter.getFinishedSpanItems())
                .as("zero sampling must result in zero exported spans")
                .isEmpty();
    }

    @TestConfiguration
    static class TestTracingConfig {

        @Bean
        InMemorySpanExporter inMemorySpanExporter() {
            return InMemorySpanExporter.create();
        }

        @Bean
        SpanExporter spanExporterAdapter(InMemorySpanExporter inMemorySpanExporter) {
            return inMemorySpanExporter;
        }

        @Bean
        SpanProcessor simpleSpanProcessor(SpanExporter spanExporterAdapter) {
            return SimpleSpanProcessor.create(spanExporterAdapter);
        }
    }
}
