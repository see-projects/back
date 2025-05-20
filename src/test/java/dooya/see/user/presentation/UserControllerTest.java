package dooya.see.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.UserFixture;
import dooya.see.user.domain.User;
import dooya.see.user.infrastructure.UserJpaRepository;
import dooya.see.user.presentation.dto.PasswordUpdateRequest;
import dooya.see.user.presentation.dto.UserSignUpRequest;
import dooya.see.user.presentation.dto.UserUpdateRequest;
import jakarta.servlet.http.Cookie;
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

import static dooya.see.common.UserFixture.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("classpath:init.sql")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = userJpaRepository.save(UserFixture.mockUser(passwordEncoder));
        testToken = jwtUtil.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
    }

    @DisplayName("유저 회원가입 성공 테스트")
    @Test
    void user_signUp_success() throws Exception {
        UserSignUpRequest request = signUpRequest();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.nickName").value(request.nickName()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @DisplayName("유저 회원가입 실패 테스트 - 개별 필드 유효성 검증")
    @ParameterizedTest(name = "{index} => 필드 = {0}, 메시지 = {1}")
    @MethodSource("invalidFieldProvider")
    void user_signUp_fail_invalidField(UserSignUpRequest request, String field, String message) throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validationErrors[0].field").value(field))
                .andExpect(jsonPath("$.validationErrors[0].message").value(message));
    }

    @DisplayName("유저 회원가입 실패 테스트 - 모든 필드가 공란일때")
    @Test
    void user_signUp_fail_allFieldsInvalid() throws Exception {
        UserSignUpRequest request = signUpRequest().toBuilder()
                .email("")
                .name("")
                .password("")
                .nickName("")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='email')].message").value("이메일은 비어 있을 수 없습니다"))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='name')].message").value("이름은 비어 있을 수 없습니다"))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='password')].message").value("비밀번호는 비어 있을 수 없습니다"))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='nickName')].message").value("닉네임은 비어 있을 수 없습니다"));
    }

    private static Stream<Arguments> invalidFieldProvider() {
        return Stream.of(
                Arguments.of(
                        signUpRequest().toBuilder().email("").build(),
                        "email", "이메일은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        signUpRequest().toBuilder().name("").build(),
                        "name", "이름은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        signUpRequest().toBuilder().password("").build(),
                        "password", "비밀번호는 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        signUpRequest().toBuilder().nickName("").build(),
                        "nickName", "닉네임은 비어 있을 수 없습니다"
                )
        );
    }

    @DisplayName("토큰에 포함된 이메일로 유저 조회 성공테스트")
    @Test
    void findByToken_user_success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.nickName").value(testUser.getNickName()))
                .andExpect(jsonPath("$.role").value(testUser.getRole().getRoleName()));
    }

    @DisplayName("토큰에 포함된 이메일로 유저 조회 실패 테스트")
    @Test
    void findByToken_user_fail() throws Exception {
        // Arrange
        String testToken = jwtUtil.createAccessToken(testUser.getId(), "test@fail.com", testUser.getRole());

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @DisplayName("이메일 중복 확인 성공 테스트")
    @Test
    void findByEmail_user_success() throws Exception {
        // Arrange
        String email = "dooya@see.com";

        // Act && Assert
        mockMvc.perform(get("/api/users/check-email")
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("이메일로 중복 확인 실패 테스트")
    @Test
    void findByEmail_user_fail() throws Exception {
        // Arrange
        String email = "test@see.com";

        // Act && Assert
        mockMvc.perform(get("/api/users/check-email")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자입니다."));
    }

    @DisplayName("유저 정보 수정 성공 테스트")
    @Test
    void userNickName_update_success() throws Exception {
        // Arrange
        UserUpdateRequest request = nickNameUpdateRequest();

        // Act && Assert
        mockMvc.perform(put("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickName").value(request.nickName()));
    }

    @DisplayName("유저 정보 수정 실패 테스트")
    @Test
    void userNickName_update_fail() throws Exception {
        // Arrange
        UserUpdateRequest request = nickNameUpdateRequest();
        String testToken = jwtUtil.createAccessToken(testUser.getId(), "test@fail.com", testUser.getRole());

        // Act & Assert
        mockMvc.perform(put("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @DisplayName("유저 비밀번호 업데이트 성공 테스트")
    @Test
    void userPassword_Update_success() throws Exception {
        // Arrange
        PasswordUpdateRequest request = passwordUpdateRequest();

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));
    }

    @DisplayName("유저 비밀번호 업데이트 실패 테스트")
    @Test
    void userPassword_Update_fail() throws Exception {
        // Arrange
        PasswordUpdateRequest request = passwordUpdateRequestFail();

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호 정보가 일치하지 않습니다."));

    }
}