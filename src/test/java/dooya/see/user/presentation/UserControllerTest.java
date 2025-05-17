package dooya.see.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.util.JwtUtil;
import dooya.see.user.domain.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static dooya.see.common.UserFixture.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @DisplayName("유저 회원가입 성공 테스트")
    @Test
    void user_signUp_success() throws Exception {
        UserSignUpRequest request = request();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.birthDate").value(request.birthDate().toString()))
                .andExpect(jsonPath("$.phoneNumber").value(request.phoneNumber()))
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
        UserSignUpRequest request = request().toBuilder()
                .email("")
                .name("")
                .password("")
                .birthDate(null)
                .phoneNumber("")
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
                .andExpect(jsonPath("$.validationErrors[?(@.field=='birthDate')].message").value("생년월일은 비어 있을 수 없습니다"))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='phoneNumber')].message").value("전화번호는 비어 있을 수 없습니다"));
    }

    private static Stream<Arguments> invalidFieldProvider() {
        return Stream.of(
                Arguments.of(
                        request().toBuilder().email("").build(),
                        "email", "이메일은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        request().toBuilder().name("").build(),
                        "name", "이름은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        request().toBuilder().password("").build(),
                        "password", "비밀번호는 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        request().toBuilder().birthDate(null).build(),
                        "birthDate", "생년월일은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        request().toBuilder().phoneNumber("").build(),
                        "phoneNumber", "전화번호는 비어 있을 수 없습니다"
                )
        );
    }

    @DisplayName("토큰으로 유저 정보 조회 성공 테스트")
    @Test
    void findByToken_user_success() throws Exception {
        // Arrange
        User testUser = testUser();
        String testToken = jwtUtil.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole());

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}