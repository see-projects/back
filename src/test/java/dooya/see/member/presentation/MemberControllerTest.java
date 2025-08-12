package dooya.see.member.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.config.SecurityConfig;
import dooya.see.auth.domain.LoginUser;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.UserFixture;
import dooya.see.member.application.dto.PasswordUpdateCommand;
import dooya.see.member.application.dto.NickNameUpdateCommand;
import dooya.see.member.application.service.MemberQueryService;
import dooya.see.member.application.service.MemberRegisterService;
import dooya.see.member.application.service.MemberUpdateService;
import dooya.see.member.application.service.MemberValidator;
import dooya.see.member.domain.Member;
import dooya.see.member.presentation.dto.PasswordUpdateRequest;
import dooya.see.member.presentation.dto.NickNameUpdateRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static dooya.see.common.UserFixture.nickNameUpdateRequest;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MemberQueryService memberQueryService;

    @MockitoBean
    private MemberRegisterService memberRegisterService;

    @MockitoBean
    private MemberUpdateService memberUpdateService;

    @MockitoBean
    private MemberValidator memberValidator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private String testToken;

    @BeforeEach
    void setUp() {
        Member testMember = UserFixture.mockUser(passwordEncoder);
        testToken = jwtUtil.createAccessToken(testMember.getId(), testMember.getEmail(), testMember.getRole());
    }

    @DisplayName("GET 요청 시 userQueryService.getUserByEmail(email) 호출 여부 검증")
    @Test
    void get_WhenCalled_InvokeGetUserByEmail() throws Exception {
        // given
        LoginUser loginUser = new LoginUser(1L, "email", "USER");

        given(memberQueryService.getUserByEmail(anyString())).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(get("/api/members")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(loginUser)))
                .andExpect(status().isOk());

        // then
        then(memberQueryService).should().getUserByEmail("email");
    }

    @DisplayName("PUT 요청 시 userUpdateService.updateNickName(email, command) 호출 여부 검증")
    @Test
    void put_WhenCalled_InvokeUpdateNickName() throws Exception {
        // given
        String email = "test@see.com";
        LoginUser loginUser = new LoginUser(1L, "test@see.com", "USER");
        NickNameUpdateRequest request = UserFixture.nickNameUpdateRequest();
        NickNameUpdateCommand command = UserFixture.updateCommand();

        given(memberUpdateService.updateNickName(anyString(), any(NickNameUpdateCommand.class))).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(put("/api/members/nick-name")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(loginUser)))
                .andExpect(status().isOk());

        // then
        then(memberUpdateService).should().updateNickName(email, command);
    }

    @DisplayName("/password 경로로 PUT 요청 시 userUpdateService.updateUserPassword(email, command) 호출 여부 검증")
    @Test
    void put_WhenCalled_InvokeUpdateUserPassword() throws Exception {
        // given
        String email = "test@see.com";
        LoginUser loginUser = new LoginUser(1L, "test@see.com", "USER");
        PasswordUpdateRequest request = UserFixture.passwordUpdateRequest();
        PasswordUpdateCommand command = UserFixture.passwordUpdateCommand();

        given(memberUpdateService.updatePassword(anyString(), any(PasswordUpdateCommand.class))).willReturn(UserFixture.passwordUpdateResult());

        // when
        mockMvc.perform(put("/api/members/password")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(loginUser)))
                .andExpect(status().isOk());

        // then
        then(memberUpdateService).should().updatePassword(email, command);
    }

    @DisplayName("/profile-image 경로로 PATCH 요청 시 userUpdateService.updateProfileImage(email, profileImage) 호출 여부 검증")
    @Test
    void patch_WhenCalled_InvokeUpdateProfileImage() throws Exception {
        // given
        String email = "test@see.com";
        LoginUser loginUser = new LoginUser(1L, "test@see.com", "USER");

        MockMultipartFile mockImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        given(memberUpdateService.updateProfileImage(anyString(), any())).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(multipart("/api/members/profile-image")
                        .file(mockImage)
                        .cookie(new Cookie("Authorization", testToken))
                        .with(user(loginUser))
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isOk());

        // then
        then(memberUpdateService).should().updateProfileImage(email, mockImage);
    }

    @DisplayName("/profile 경로로 PUT 요청 시 userUpdateService.updateProfile(email, command, profileImage) 호출 여부 검증")
    @Test
    void put_WhenCalled_InvokeUpdateProfile() throws Exception {
        // given
        String email = "test@see.com";
        LoginUser loginUser = new LoginUser(1L, "test@see.com", "USER");
        NickNameUpdateCommand command = UserFixture.updateCommand();
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

        given(memberUpdateService.updateProfile(anyString(), any(), any())).willReturn(UserFixture.testUserResult());

        // when
        mockMvc.perform(multipart("/api/members/profile")
                        .file(nickNamePart)
                        .file(mockImage)
                        .cookie(new Cookie("Authorization", testToken))
                        .with(user(loginUser))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        // then
        then(memberUpdateService).should().updateProfile(email, command, mockImage);
    }
}
