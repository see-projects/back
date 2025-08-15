package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.MemberProfileResponse;
import dooya.see.adapter.webapi.dto.MemberRegisterResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.member.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

import static dooya.see.domain.member.MemberFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class MemberApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRepository memberRepository;
    final MemberRegister memberRegister;

    @Test
    @DisplayName("회원 등록 요청 시 회원 ID와 이메일이 포함된 응답을 반환하고 데이터베이스에 저장된다")
    void register() throws JsonProcessingException, UnsupportedEncodingException {
        MemberRegisterRequest request = createMemberRegisterRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.memberId", value -> assertThat(value).isNotNull())
                .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo(request.email()));

        MemberRegisterResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), MemberRegisterResponse.class);

        Member member = memberRepository.findById(response.memberId()).orElseThrow();

        assertThat(member.getEmail().address()).isEqualTo(request.email());
        assertThat(member.getNickname()).isEqualTo(request.nickname());
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("동일한 이메일로 회원 등록 시도 시 409 Conflict 상태 코드를 반환한다")
    void duplicateEmail() throws JsonProcessingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberRegisterRequest request = createMemberRegisterRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("올바른 회원 정보로 로그인 시 회원 ID와 액세스 토큰이 포함된 응답을 반환한다")
    void login() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/members/login").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo(request.email()));

        MemberAuthResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), MemberAuthResponse.class);

        Member member = memberRepository.findById(response.memberId()).orElseThrow();

        assertThat(member.getEmail().address()).isEqualTo(request.email());
        assertThat(response.accessToken()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 404 Not Found 상태 코드를 반환한다")
    void loginFailWithWrongEmail() throws JsonProcessingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest("wrong@see.com");
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/members/login").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 401 Unauthorized 상태 코드를 반환한다")
    void loginFailWithWrongPassword() throws JsonProcessingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createAuthRequestWithPassword("wrongPassword");
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/members/login").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효한 액세스 토큰으로 현재 회원 정보 조회 시 회원 정보를 반환한다")
    void getCurrentMember() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

        MvcTestResult getCurrentMemberResult = mvcTester.get().uri("/api/members/my")
                .header("Authorization", "Bearer " + authResponse.accessToken())
                .exchange();

        MemberProfileResponse profileResponse =
                objectMapper.readValue(getCurrentMemberResult.getResponse().getContentAsString(), MemberProfileResponse.class);

        Member member = memberRepository.findById(profileResponse.memberId()).orElseThrow();

        assertThat(member.getId()).isEqualTo(profileResponse.memberId());
    }

    @Test
    @DisplayName("Authorization 헤더 없이 현재 회원 정보 조회 시 401 Unauthorized 상태 코드를 반환한다")
    void getCurrentMemberFailWithoutAuthorizationHeader() {
        MvcTestResult result = mvcTester.get().uri("/api/members/my").exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("잘못된 형식의 Authorization 헤더로 현재 회원 정보 조회 시 401 Unauthorized 상태 코드를 반환한다")
    void getCurrentMemberFailWithInvalidAuthorizationHeader() {
        MvcTestResult result = mvcTester.get().uri("/api/members/my")
                .header("Authorization", "InvalidTokenFormat")
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 현재 회원 정보 조회 시 401 Unauthorized 상태 코드를 반환한다")
    void getCurrentMemberFailWithInvalidToken() {
        MvcTestResult result = mvcTester.get().uri("/api/members/my")
                .header("Authorization", "Bearer invalidToken")
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("")
    void deactivated() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

        MvcTestResult getCurrentMemberResult = mvcTester.patch().uri("/api/members/my/deactivate")
                .header("Authorization", "Bearer " + authResponse.accessToken())
                .exchange();

        assertThat(getCurrentMemberResult).hasStatusOk();
    }
}
