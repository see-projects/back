package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostFixture;
import dooya.see.domain.post.PostStatus;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

import static dooya.see.domain.member.MemberFixture.createMemberAuthRequest;
import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class PostApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRegister memberRegister;
    
    @Test
    @DisplayName("")
    void a() throws JsonProcessingException, UnsupportedEncodingException {
        String token = createMemberAndGetToken();

        PostCreateRequest request = PostFixture.createPostRequest(true);
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result).hasStatusOk();

        PostCreateResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostCreateResponse.class);

        assertThat(response.postId()).isNotNull();
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo(request.category());
        assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.createdAt()).isNotNull();

    }

    private String createMemberAndGetToken() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

        return authResponse.accessToken();
    }
}