package in.vis.controller;

import in.vis.dto.UserResponse;
import in.vis.enums.Role;
import in.vis.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;

    @Test
    void get_me_returns_200_with_user_for_authenticated_uid() throws Exception {
        UserResponse user = new UserResponse(
                10L, "uid-abc", "Aarav", "a@b.c", null,
                Role.CLIENT, 1L, "Kandivali", true);
        when(userService.getByFirebaseUid("uid-abc")).thenReturn(user);

        var auth = new UsernamePasswordAuthenticationToken("uid-abc", null, Collections.emptyList());

        mockMvc.perform(get("/auth/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firebaseUid").value("uid-abc"))
                .andExpect(jsonPath("$.name").value("Aarav"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.branchId").value(1))
                .andExpect(jsonPath("$.branchName").value("Kandivali"));
    }

    @Test
    void get_me_returns_401_when_unauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
