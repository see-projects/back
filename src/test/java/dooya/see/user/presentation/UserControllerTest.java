package dooya.see.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.config.SecurityConfig;
import dooya.see.auth.domain.LoginUser;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.UserFixture;
import dooya.see.user.application.dto.PasswordUpdateCommand;
import dooya.see.user.application.dto.UserSignUpCommand;
import dooya.see.user.application.dto.UserUpdateCommand;
import dooya.see.user.application.service.UserQueryService;
import dooya.see.user.application.service.UserSignUpService;
import dooya.see.user.application.service.UserUpdateService;
import dooya.see.user.application.service.UserValidator;
import dooya.see.user.domain.User;
import dooya.see.user.presentation.dto.PasswordUpdateRequest;
import dooya.see.user.presentation.dto.UserSignUpRequest;
import dooya.see.user.presentation.dto.UserUpdateRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static dooya.see.common.UserFixture.signUpRequest;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private UserSignUpService userSignUpService;

    @MockitoBean
    private UserUpdateService userUpdateService;

    @MockitoBean
    private UserValidator userValidator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private String testToken;

    @BeforeEach
    void setUp() {
        User testUser = UserFixture.mockUser(passwordEncoder);
        testToken = jwtUtil.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
    }

    @DisplayName("POST 요청 시 userSignUpService.userSignUp() 호출 여부 검증")
    @Test
    void post_WhenCalled_InvokesUserSignUp() throws Exception {
        // given
        UserSignUpRequest request = signUpRequest();
        UserSignUpCommand command = UserFixture.signUpCommand();

        given(userSignUpService.userSignUp(any(UserSignUpCommand.class))).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // then
        then(userSignUpService).should().userSignUp(command);
    }

    @DisplayName("GET 요청 시 userQueryService.getUserByEmail(email) 호출 여부 검증")
    @Test
    void get_WhenCalled_InvokeGetUserByEmail() throws Exception {
        // given
        LoginUser loginUser = new LoginUser(1L, "email", "USER");

        given(userQueryService.getUserByEmail(anyString())).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(get("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(loginUser)))
                .andExpect(status().isOk());

        // then
        then(userQueryService).should().getUserByEmail("email");
    }

    @DisplayName("/check-email 경로로 GET 요청 시 userValidator.validateDuplicateEmail(email) 호출 여부 검증")
    @Test
    void get_WhenCalled_InvokeValidateDuplicateEmail() throws Exception {
        // given
        String email = "email";

        // when
        mockMvc.perform(get("/api/users/check-email")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        then(userValidator).should().validateDuplicateEmail(email);
    }

    @DisplayName("PUT 요청 시 userUpdateService.updateNickName(email, command) 호출 여부 검증")
    @Test
    void put_WhenCalled_InvokeUpdateNickName() throws Exception {
        // given
        String email = "test@see.com";
        LoginUser loginUser = new LoginUser(1L, "test@see.com", "USER");
        UserUpdateRequest request = UserFixture.nickNameUpdateRequest();
        UserUpdateCommand command = UserFixture.updateCommand();

        given(userUpdateService.updateNickName(anyString(), any(UserUpdateCommand.class))).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(put("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(loginUser)))
                .andExpect(status().isOk());

        // then
        then(userUpdateService).should().updateNickName(email, command);
    }

    @DisplayName("/password 경로로 PUT 요청 시 userUpdateService.updateUserPassword(email, command) 호출 여부 검증")
    @Test
    void put_WhenCalled_InvokeUpdateUserPassword() throws Exception {
        // given
        String email = "test@see.com";
        LoginUser loginUser = new LoginUser(1L, "test@see.com", "USER");
        PasswordUpdateRequest request = UserFixture.passwordUpdateRequest();
        PasswordUpdateCommand command = UserFixture.passwordUpdateCommand();

        given(userUpdateService.updatePassword(anyString(), any(PasswordUpdateCommand.class))).willReturn(UserFixture.passwordUpdateResult());

        // when
        mockMvc.perform(put("/api/users/password")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(loginUser)))
                .andExpect(status().isOk());

        // then
        then(userUpdateService).should().updatePassword(email, command);
    }
}
