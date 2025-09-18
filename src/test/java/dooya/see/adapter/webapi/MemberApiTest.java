package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.MemberProfileResponse;
import dooya.see.adapter.webapi.dto.MemberRegisterResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.MemberRepository;
import dooya.see.domain.member.*;
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

    @Nested
    class 회원_등록 {
        @Test
        void 회원_등록_요청_시_회원_ID와_이메일이_포함된_응답을_반환하고_데이터베이스에_저장된다() throws JsonProcessingException, UnsupportedEncodingException {
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
        void 동일한_이메일로_회원_등록_시도_시_409_Conflict_상태_코드를_반환한다() throws JsonProcessingException {
            memberRegister.register(createMemberRegisterRequest());

            MemberRegisterRequest request = createMemberRegisterRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/members").contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class 회원_로그인 {
        @Test
        void 올바른_회원_정보로_로그인_시_회원_ID와_액세스_토큰이_포함된_응답을_반환한다() throws JsonProcessingException, UnsupportedEncodingException {
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
        void 존재하지_않는_이메일로_로그인_시도_시_404_Not_Found_상태_코드를_반환한다() throws JsonProcessingException {
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
        void 잘못된_비밀번호로_로그인_시도_시_401_Unauthorized_상태_코드를_반환한다() throws JsonProcessingException {
            memberRegister.register(createMemberRegisterRequest());

            MemberAuthRequest request = createAuthRequestWithPassword("wrongPassword");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/members/login").contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class 현재_회원_정보_조회 {
        @Test
        void 유효한_액세스_토큰으로_현재_회원_정보_조회_시_회원_정보를_반환한다() throws JsonProcessingException, UnsupportedEncodingException {
            memberRegister.register(createMemberRegisterRequest());

            MemberAuthRequest request = createMemberAuthRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            MemberAuthResponse authResponse =
                    objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

            MvcTestResult getCurrentMemberResult = mvcTester.get().uri("/api/members/me")
                    .header("Authorization", "Bearer " + authResponse.accessToken())
                    .exchange();

            MemberProfileResponse profileResponse =
                    objectMapper.readValue(getCurrentMemberResult.getResponse().getContentAsString(), MemberProfileResponse.class);

            Member member = memberRepository.findById(profileResponse.memberId()).orElseThrow();

            assertThat(member.getId()).isEqualTo(profileResponse.memberId());
        }

        @Test
        void Authorization_헤더_없이_현재_회원_정보_조회_시_401_Unauthorized_상태_코드를_반환한다() {
            MvcTestResult result = mvcTester.get().uri("/api/members/me").exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 잘못된_형식의_Authorization_헤더로_현재_회원_정보_조회_시_401_Unauthorized_상태_코드를_반환한다() {
            MvcTestResult result = mvcTester.get().uri("/api/members/me")
                    .header("Authorization", "InvalidTokenFormat")
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 유효하지_않은_토큰으로_현재_회원_정보_조회_시_401_Unauthorized_상태_코드를_반환한다() {
            MvcTestResult result = mvcTester.get().uri("/api/members/me")
                    .header("Authorization", "Bearer invalidToken")
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class 회원_탈퇴 {
        @Test
        void 유효한_토큰으로_회원_탈퇴_요청_시_회원_상태가_DEACTIVATED로_변경되고_성공_응답을_반환한다() throws JsonProcessingException, UnsupportedEncodingException {
            memberRegister.register(createMemberRegisterRequest());

            MemberAuthRequest request = createMemberAuthRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            MemberAuthResponse authResponse =
                    objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

            MvcTestResult result = mvcTester.patch().uri("/api/members/my/deactivate")
                    .header("Authorization", "Bearer " + authResponse.accessToken())
                    .exchange();

            assertThat(result).hasStatusOk();

            Member member = memberRepository.findById(authResponse.memberId()).orElseThrow();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        }

        @Test
        void Authorization_헤더_없이_회원_탈퇴_요청_시_401_Unauthorized_상태_코드를_반환한다() {
            MvcTestResult result = mvcTester.patch().uri("/api/members/my/deactivate").exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 잘못된_형식의_Authorization_헤더로_회원_탈퇴_요청_시_401_Unauthorized_상태_코드를_반환한다() {
            MvcTestResult result = mvcTester.patch().uri("/api/members/my/deactivate")
                    .header("Authorization", "InvalidTokenFormat")
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 유효하지_않은_토큰으로_회원_탈퇴_요청_시_401_Unauthorized_상태_코드를반환한다() {
            MvcTestResult result = mvcTester.patch().uri("/api/members/my/deactivate")
                    .header("Authorization", "Bearer invalidToken")
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class 회원_정보_수정 {
        @Test
        void 유효한_토큰으로_회원_정보_수정_요청_시_회원_정보가_변경되고_성공_응답을_반환한다() throws JsonProcessingException, UnsupportedEncodingException {
            memberRegister.register(createMemberRegisterRequest());

            MemberAuthRequest request = createMemberAuthRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            MemberAuthResponse authResponse =
                    objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

            MemberInfoUpdateRequest updateInfoRequest = createMemberInfoUpdateRequest();
            String updateRequestJson = objectMapper.writeValueAsString(updateInfoRequest);

            MvcTestResult result = mvcTester.put().uri("/api/members/me")
                    .header("Authorization", "Bearer " + authResponse.accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequestJson)
                    .exchange();

            assertThat(result).hasStatusOk();

            Member member = memberRepository.findById(authResponse.memberId()).orElseThrow();
            assertThat(member.getNickname()).isEqualTo(updateInfoRequest.nickname());
            assertThat(member.getDetail().getProfile().address()).isEqualTo(updateInfoRequest.profileAddress());
            assertThat(member.getDetail().getIntroduction()).isEqualTo(updateInfoRequest.introduction());
        }

        @Test
        void 본문_없이_회원_정보_수정_요청_시_400_Bad_Request_상태_코드를_반환한다() {
            MvcTestResult result = mvcTester.put().uri("/api/members/me").exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void Authorization_헤더_없이_회원_정보_수정_요청_시_401_Unauthorized_상태_코드를_반환한다() throws JsonProcessingException {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/members/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 잘못된_형식의_Authorization_헤더로_회원_정보_수정_요청_시_401_Unauthorized_상태_코드를_반환한다() throws JsonProcessingException {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/members/me")
                    .header("Authorization", "InvalidTokenFormat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 유효하지_않은_토큰으로_회원_정보_수정_요청_시_401_Unauthorized_상태_코드를_반환한다() throws JsonProcessingException {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/members/me")
                    .header("Authorization", "Bearer invalidToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }
}
