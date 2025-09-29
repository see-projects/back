package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.MemberProfileResponse;
import dooya.see.adapter.webapi.dto.MemberRegisterResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.*;
import dooya.see.domain.member.dto.MemberAuthRequest;
import dooya.see.domain.member.dto.MemberInfoUpdateRequest;
import dooya.see.domain.member.dto.MemberRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class MemberApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRepository memberRepository;
    final MemberRegister memberRegister;

    private static final String VALID_TOKEN_PREFIX = "Bearer ";
    private static final String INVALID_TOKEN = "invalidToken";
    private static final String INVALID_TOKEN_FORMAT = "InvalidTokenFormat";
    private static final String WRONG_EMAIL = "wrong@see.com";
    private static final String WRONG_PASSWORD = "wrongPassword";

    @Nested
    class 회원_등록 {
        @Test
        void 회원_등록이_성공한다() throws JsonProcessingException, UnsupportedEncodingException {
            MemberRegisterRequest request = createMemberRegisterRequest();

            MvcTestResult result = performMemberRegister(request);

            assertThatMemberRegistered(result, request);
        }

        @Test
        void 동일한_이메일로_회원_등록_시_409_Conflict가_발생한다() throws JsonProcessingException {
            registerTestMember();
            MemberRegisterRequest request = createMemberRegisterRequest();

            MvcTestResult result = performMemberRegister(request);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        private void assertThatMemberRegistered(MvcTestResult result, MemberRegisterRequest request)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .hasPathSatisfying("$.memberId", value -> assertThat(value).isNotNull())
                    .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo(request.email()));

            MemberRegisterResponse response = parseResponse(result, MemberRegisterResponse.class);
            Member member = memberRepository.findById(response.memberId()).orElseThrow();

            assertThat(member.getEmail().address()).isEqualTo(request.email());
            assertThat(member.getNickname()).isEqualTo(request.nickname());
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }
    }

    @Nested
    class 회원_로그인 {
        @Test
        void 올바른_회원_정보로_로그인이_성공한다() throws JsonProcessingException, UnsupportedEncodingException {
            registerTestMember();
            MemberAuthRequest request = createMemberAuthRequest();

            MvcTestResult result = performMemberLogin(request);

            assertThatMemberLoggedIn(result, request);
        }

        @Test
        void 존재하지_않는_이메일로_로그인_시_404_Not_Found가_발생한다() throws JsonProcessingException {
            registerTestMember();
            MemberAuthRequest request = createMemberAuthRequest(WRONG_EMAIL);

            MvcTestResult result = performMemberLogin(request);

            assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        }

        @Test
        void 잘못된_비밀번호로_로그인_시_401_Unauthorized가_발생한다() throws JsonProcessingException {
            registerTestMember();
            MemberAuthRequest request = createAuthRequestWithPassword(WRONG_PASSWORD);

            MvcTestResult result = performMemberLogin(request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        private void assertThatMemberLoggedIn(MvcTestResult result, MemberAuthRequest request)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo(request.email()));

            MemberAuthResponse response = parseResponse(result, MemberAuthResponse.class);
            Member member = memberRepository.findById(response.memberId()).orElseThrow();

            assertThat(member.getEmail().address()).isEqualTo(request.email());
            assertThat(response.accessToken()).isNotNull();
        }
    }

    @Nested
    class 현재_회원_정보_조회 {
        @Test
        void 유효한_토큰으로_현재_회원_정보_조회가_성공한다() throws JsonProcessingException, UnsupportedEncodingException {
            String accessToken = registerAndLoginTestMember();

            MvcTestResult result = performGetCurrentMember(accessToken);

            assertThatCurrentMemberRetrieved(result);
        }

        @Test
        void Authorization_헤더_없이_현재_회원_정보_조회_시_401_Unauthorized가_발생한다() {
            MvcTestResult result = performGetCurrentMemberWithoutAuth();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 잘못된_형식의_Authorization_헤더로_현재_회원_정보_조회_시_401_Unauthorized가_발생한다() {
            MvcTestResult result = performGetCurrentMemberWithInvalidFormat();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 유효하지_않은_토큰으로_현재_회원_정보_조회_시_401_Unauthorized가_발생한다() {
            MvcTestResult result = performGetCurrentMemberWithInvalidToken();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        private void assertThatCurrentMemberRetrieved(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            MemberProfileResponse profileResponse = parseResponse(result, MemberProfileResponse.class);
            Member member = memberRepository.findById(profileResponse.memberId()).orElseThrow();

            assertThat(member.getId()).isEqualTo(profileResponse.memberId());
        }
    }

    @Nested
    class 회원_탈퇴 {
        @Test
        void 유효한_토큰으로_회원_탈퇴가_성공한다() throws JsonProcessingException, UnsupportedEncodingException {
            String accessToken = registerAndLoginTestMember();
            Long memberId = getMemberIdFromToken(accessToken);

            MvcTestResult result = performMemberDeactivate(accessToken);

            assertThatMemberDeactivated(result, memberId);
        }

        @Test
        void Authorization_헤더_없이_회원_탈퇴_시_401_Unauthorized가_발생한다() {
            MvcTestResult result = performMemberDeactivateWithoutAuth();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 잘못된_형식의_Authorization_헤더로_회원_탈퇴_시_401_Unauthorized가_발생한다() {
            MvcTestResult result = performMemberDeactivateWithInvalidFormat();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 유효하지_않은_토큰으로_회원_탈퇴_시_401_Unauthorized가_발생한다() {
            MvcTestResult result = performMemberDeactivateWithInvalidToken();

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        private void assertThatMemberDeactivated(MvcTestResult result, Long memberId) {
            assertThat(result).hasStatusOk();

            Member member = memberRepository.findById(memberId).orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        }
    }

    @Nested
    class 회원_정보_수정 {
        @Test
        void 유효한_토큰으로_회원_정보_수정이_성공한다() throws JsonProcessingException, UnsupportedEncodingException {
            String accessToken = registerAndLoginTestMember();
            Long memberId = getMemberIdFromToken(accessToken);
            MemberInfoUpdateRequest updateRequest = createMemberInfoUpdateRequest();

            MvcTestResult result = performMemberInfoUpdate(accessToken, updateRequest);

            assertThatMemberInfoUpdated(result, memberId, updateRequest);
        }

        @Test
        void 본문_없이_회원_정보_수정_시_400_Bad_Request가_발생한다() {
            MvcTestResult result = performMemberInfoUpdateWithoutBody();

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void Authorization_헤더_없이_회원_정보_수정_시_401_Unauthorized가_발생한다() throws JsonProcessingException {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();

            MvcTestResult result = performMemberInfoUpdateWithoutAuth(request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 잘못된_형식의_Authorization_헤더로_회원_정보_수정_시_401_Unauthorized가_발생한다() throws JsonProcessingException {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();

            MvcTestResult result = performMemberInfoUpdateWithInvalidFormat(request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 유효하지_않은_토큰으로_회원_정보_수정_시_401_Unauthorized가_발생한다() throws JsonProcessingException {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();

            MvcTestResult result = performMemberInfoUpdateWithInvalidToken(request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        private void assertThatMemberInfoUpdated(MvcTestResult result, Long memberId, MemberInfoUpdateRequest updateRequest) {
            assertThat(result).hasStatusOk();

            Member member = memberRepository.findById(memberId).orElseThrow();
            assertThat(member.getNickname()).isEqualTo(updateRequest.nickname());
            assertThat(member.getDetail().getProfile().address()).isEqualTo(updateRequest.profileAddress());
            assertThat(member.getDetail().getIntroduction()).isEqualTo(updateRequest.introduction());
        }
    }

    // 헬퍼 메서드들
    private void registerTestMember() {
        memberRegister.register(createMemberRegisterRequest());
    }

    private String registerAndLoginTestMember() throws JsonProcessingException, UnsupportedEncodingException {
        registerTestMember();
        MemberAuthRequest request = createMemberAuthRequest();
        MvcTestResult loginResult = performMemberLogin(request);
        MemberAuthResponse authResponse = parseResponse(loginResult, MemberAuthResponse.class);
        return authResponse.accessToken();
    }

    private Long getMemberIdFromToken(String accessToken) throws JsonProcessingException, UnsupportedEncodingException {
        MvcTestResult result = performGetCurrentMember(accessToken);
        MemberProfileResponse response = parseResponse(result, MemberProfileResponse.class);
        return response.memberId();
    }

    // API 호출 헬퍼 메서드들
    private MvcTestResult performMemberRegister(MemberRegisterRequest request) throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.post().uri("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performMemberLogin(MemberAuthRequest request) throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performGetCurrentMember(String accessToken) {
        return mvcTester.get().uri("/api/members/me")
                .header("Authorization", VALID_TOKEN_PREFIX + accessToken)
                .exchange();
    }

    private MvcTestResult performGetCurrentMemberWithoutAuth() {
        return mvcTester.get().uri("/api/members/me").exchange();
    }

    private MvcTestResult performGetCurrentMemberWithInvalidFormat() {
        return mvcTester.get().uri("/api/members/me")
                .header("Authorization", INVALID_TOKEN_FORMAT)
                .exchange();
    }

    private MvcTestResult performGetCurrentMemberWithInvalidToken() {
        return mvcTester.get().uri("/api/members/me")
                .header("Authorization", VALID_TOKEN_PREFIX + INVALID_TOKEN)
                .exchange();
    }

    private MvcTestResult performMemberDeactivate(String accessToken) {
        return mvcTester.patch().uri("/api/members/my/deactivate")
                .header("Authorization", VALID_TOKEN_PREFIX + accessToken)
                .exchange();
    }

    private MvcTestResult performMemberDeactivateWithoutAuth() {
        return mvcTester.patch().uri("/api/members/my/deactivate").exchange();
    }

    private MvcTestResult performMemberDeactivateWithInvalidFormat() {
        return mvcTester.patch().uri("/api/members/my/deactivate")
                .header("Authorization", INVALID_TOKEN_FORMAT)
                .exchange();
    }

    private MvcTestResult performMemberDeactivateWithInvalidToken() {
        return mvcTester.patch().uri("/api/members/my/deactivate")
                .header("Authorization", VALID_TOKEN_PREFIX + INVALID_TOKEN)
                .exchange();
    }

    private MvcTestResult performMemberInfoUpdate(String accessToken, MemberInfoUpdateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/members/me")
                .header("Authorization", VALID_TOKEN_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performMemberInfoUpdateWithoutBody() {
        return mvcTester.put().uri("/api/members/me").exchange();
    }

    private MvcTestResult performMemberInfoUpdateWithoutAuth(MemberInfoUpdateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performMemberInfoUpdateWithInvalidFormat(MemberInfoUpdateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/members/me")
                .header("Authorization", INVALID_TOKEN_FORMAT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performMemberInfoUpdateWithInvalidToken(MemberInfoUpdateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/members/me")
                .header("Authorization", VALID_TOKEN_PREFIX + INVALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    // 응답 파싱 헬퍼 메서드
    private <T> T parseResponse(MvcTestResult result, Class<T> responseType)
            throws UnsupportedEncodingException, JsonProcessingException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }
}