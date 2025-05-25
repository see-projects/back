package dooya.see.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.service.AuthService;
import dooya.see.auth.config.SecurityConfig;
import dooya.see.auth.presentation.dto.LoginRequest;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.AuthFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @DisplayName("/login 경로로 POST 요청 시 authService.login(command) 호출 여부 검증")
    @Test
    void post_WhenCalled_InvokeLogin() throws Exception {
        // given
        LoginRequest request = AuthFixture.request();
        LoginCommand command = AuthFixture.command();

        given(authService.login(any(LoginCommand.class))).willReturn(AuthFixture.result());

        // when
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        then(authService).should().login(command);
    }
}
