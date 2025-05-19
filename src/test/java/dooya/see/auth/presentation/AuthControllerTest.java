package dooya.see.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.presentation.dto.LoginRequest;
import dooya.see.common.UserFixture;
import dooya.see.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static dooya.see.common.AuthFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("classpath:init.sql")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userJpaRepository.save(UserFixture.mockUser(passwordEncoder));
    }

    @DisplayName("유저 로그인 성공 테스트")
    @Test
    void user_login_success() throws Exception {
        // Arrange
        LoginRequest request = request();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(result -> {
                    String cookie = result.getResponse().getHeader("Set-Cookie");
                    assertThat(cookie).contains("Authorization");
                });
    }

    @DisplayName("유저 로그인 실패 테스트 - 개별 필드 유효성 검증")
    @ParameterizedTest(name = "{index} => 필드 = {0}, 메시지 = {1}")
    @MethodSource("invalidFieldProvider")
    void user_login_fail_invalidField(LoginRequest request, String field, String message) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validationErrors[0].field").value(field))
                .andExpect(jsonPath("$.validationErrors[0].message").value(message));
    }

    private static Stream<Arguments> invalidFieldProvider() {
        return Stream.of(
                Arguments.of(
                        request().toBuilder().email("").build(),
                        "email", "이메일은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        request().toBuilder().password("").build(),
                        "password", "비밀번호는 비어 있을 수 없습니다"
                )
        );
    }
}
