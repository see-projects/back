package dooya.see.member.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.UserFixture;
import dooya.see.member.domain.Member;
import dooya.see.member.infrastructure.MemberJpaRepository;
import dooya.see.member.presentation.dto.PasswordUpdateRequest;
import dooya.see.member.presentation.dto.MemberRegisterRequest;
import dooya.see.member.presentation.dto.NickNameUpdateRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static dooya.see.common.UserFixture.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("classpath:init.sql")
class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member testMember;
    private String testToken;

    @BeforeEach
    void setUp() {
        testMember = memberJpaRepository.save(UserFixture.mockUser(passwordEncoder));
        testToken = jwtUtil.createAccessToken(testMember.getId(), testMember.getEmail(), testMember.getRole());
    }

    @DisplayName("토큰에 포함된 이메일로 유저 조회 성공테스트")
    @Test
    void findByToken_User_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/members")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testMember.getId()))
                .andExpect(jsonPath("$.email").value(testMember.getEmail()))
                .andExpect(jsonPath("$.name").value(testMember.getName()))
                .andExpect(jsonPath("$.nickName").value(testMember.getNickName()))
                .andExpect(jsonPath("$.role").value(testMember.getRole().getRoleName()));
    }

    @DisplayName("토큰에 포함된 이메일로 유저 조회 실패 테스트")
    @Test
    void findByToken_User_Fail() throws Exception {
        // Arrange
        String testToken = jwtUtil.createAccessToken(testMember.getId(), "test@fail.com", testMember.getRole());

        // Act & Assert
        mockMvc.perform(get("/api/members")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @DisplayName("유저 정보 수정 성공 테스트")
    @Test
    void userNickName_Update_Success() throws Exception {
        // Arrange
        NickNameUpdateRequest request = nickNameUpdateRequest();

        // Act && Assert
        mockMvc.perform(put("/api/members/nick-name")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickName").value(request.nickName()));
    }

    @DisplayName("유저 정보 수정 실패 테스트")
    @Test
    void userNickName_Update_Fail() throws Exception {
        // Arrange
        NickNameUpdateRequest request = nickNameUpdateRequest();
        String testToken = jwtUtil.createAccessToken(testMember.getId(), "test@fail.com", testMember.getRole());

        // Act & Assert
        mockMvc.perform(put("/api/members/nick-name")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @DisplayName("유저 비밀번호 업데이트 성공 테스트")
    @Test
    void userPassword_Update_Success() throws Exception {
        // Arrange
        PasswordUpdateRequest request = passwordUpdateRequest();

        // Act & Assert
        mockMvc.perform(put("/api/members/password")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));
    }

    @DisplayName("유저 비밀번호 업데이트 실패 테스트")
    @Test
    void userPassword_Update_Fail() throws Exception {
        // Arrange
        PasswordUpdateRequest request = passwordUpdateRequestFail();

        // Act & Assert
        mockMvc.perform(put("/api/members/password")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호 정보가 일치하지 않습니다."));

    }

    @DisplayName("유저 프로필 이미지 업데이트 성공 테스트")
    @Test
    void userProfileImage_Update_Success() throws Exception {
        // Arrange
        MockMultipartFile mockImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        // Act && Assert
        mockMvc.perform(multipart("/api/members/profile-image")
                        .file(mockImage)
                        .cookie(new Cookie("Authorization", testToken))
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").isNotEmpty());
    }

    @DisplayName("유저 프로필 업데이트 성공 테스트")
    @Test
    void userProfile_Update_Success() throws Exception {
        // Arrange
        String nickNameJson = objectMapper.writeValueAsString(nickNameUpdateRequest());

        MockMultipartFile nickNamePart = new MockMultipartFile(
                "request",
                null,
                MediaType.APPLICATION_JSON_VALUE,
                nickNameJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile mockImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        // Act && Assert
        mockMvc.perform(multipart("/api/members/profile")
                        .file(nickNamePart)
                        .file(mockImage)
                        .cookie(new Cookie("Authorization", testToken))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickName").value(nickNameUpdateRequest().nickName()))
                .andExpect(jsonPath("$.profileImageUrl").isNotEmpty());
    }
}