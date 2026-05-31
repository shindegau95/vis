package in.vis.controller;

import in.vis.MediaTypes;
import in.vis.config.V1ContentTypeAdvice;
import in.vis.config.WebConfig;
import in.vis.exception.GlobalExceptionHandler;
import in.vis.model.Branch;
import in.vis.repository.BranchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BranchController.class)
@Import({GlobalExceptionHandler.class, WebConfig.class, V1ContentTypeAdvice.class})
class BranchVersioningIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BranchRepository branchRepository;

    private static UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken("uid-versioning", null, Collections.emptyList());
    }

    private void seedSingleBranch() {
        Branch b = new Branch();
        b.setId(1L); b.setName("Mira Road"); b.setCity("Mumbai");
        when(branchRepository.findAll()).thenReturn(List.of(b));
    }

    @Test
    void accept_v1_returns_v1_content_type() throws Exception {
        seedSingleBranch();
        mockMvc.perform(get("/branches")
                        .with(authentication(auth()))
                        .accept(MediaTypes.APPLICATION_VND_VIS_V1_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_VND_VIS_V1_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mira Road"));
    }

    @Test
    void no_accept_header_defaults_to_v1() throws Exception {
        seedSingleBranch();
        mockMvc.perform(get("/branches").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_VND_VIS_V1_JSON_VALUE));
    }

    @Test
    void accept_plain_json_returns_v1_content_type() throws Exception {
        seedSingleBranch();
        mockMvc.perform(get("/branches")
                        .with(authentication(auth()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_VND_VIS_V1_JSON_VALUE));
    }

    @Test
    void accept_v2_returns_v2_content_type_and_body_has_slug() throws Exception {
        seedSingleBranch();
        mockMvc.perform(get("/branches")
                        .with(authentication(auth()))
                        .accept(MediaTypes.APPLICATION_VND_VIS_V2_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_VND_VIS_V2_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mira Road"))
                .andExpect(jsonPath("$[0].slug").value("mira-road"));
    }

    @Test
    void accept_v3_returns_406_not_acceptable() throws Exception {
        seedSingleBranch();
        mockMvc.perform(get("/branches")
                        .with(authentication(auth()))
                        .accept(MediaType.valueOf("application/vnd.vis.v3+json")))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void v1_response_body_does_not_contain_slug_field() throws Exception {
        seedSingleBranch();
        mockMvc.perform(get("/branches")
                        .with(authentication(auth()))
                        .accept(MediaTypes.APPLICATION_VND_VIS_V1_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").doesNotExist());
    }
}
