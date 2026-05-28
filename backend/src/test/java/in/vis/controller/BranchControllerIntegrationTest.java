package in.vis.controller;

import in.vis.exception.GlobalExceptionHandler;
import in.vis.model.Branch;
import in.vis.repository.BranchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BranchController.class)
@Import(GlobalExceptionHandler.class)
class BranchControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BranchRepository branchRepository;

    private static UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken("uid-test", null, Collections.emptyList());
    }

    @Test
    void list_returns_200_with_branch_array_for_authenticated_user() throws Exception {
        Branch a = new Branch();
        a.setId(1L); a.setName("Kandivali"); a.setCity("Mumbai");
        Branch b = new Branch();
        b.setId(2L); b.setName("Borivali"); b.setCity("Mumbai");
        when(branchRepository.findAll()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/branches").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Kandivali"))
                .andExpect(jsonPath("$[0].city").value("Mumbai"))
                .andExpect(jsonPath("$[1].name").value("Borivali"));
    }

    @Test
    void list_returns_empty_array_when_no_branches() throws Exception {
        when(branchRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/branches").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void list_returns_401_when_unauthenticated() throws Exception {
        mockMvc.perform(get("/branches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void get_returns_404_with_error_body_when_branch_unknown() throws Exception {
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/branches/999").with(authentication(auth())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Branch not found: 999"));
    }
}
