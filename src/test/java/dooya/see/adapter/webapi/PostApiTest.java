package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.PostUpdateRequest;
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

import static dooya.see.domain.member.MemberFixture.createMemberAuthRequest;
import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class PostApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRegister memberRegister;
    
    @Test
    @DisplayName("로그인한 사용자가 게시글을 생성할 수 있다")
    void createPost() throws JsonProcessingException, UnsupportedEncodingException {
        String token = createMemberAndGetToken();

        PostCreateRequest request = createPostRequest(true);
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

    @Test
    @DisplayName("토큰 없이 게시글 생성 요청 시 401 Unauthorized가 발생한다")
    void createPostWithoutToken() throws JsonProcessingException {
        PostCreateRequest request = createPostRequest(true);
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("게시글 조회 시 조회수가 1 증가한다")
    void getPostIncreasesViewCount() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.viewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인한 다른 사용자가 게시글을 조회할 수 있다")
    void getPostWithDifferentUser() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.viewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("작성자가 게시글을 수정할 수 있다")
    void updatePostByAuthor() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        PostUpdateRequest request = updateAllFieldsRequest();

        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.put().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.body()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("토큰 없이 게시글 수정 요청 시 401 Unauthorized가 발생한다")
    void updatePostWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        PostUpdateRequest request = updateAllFieldsRequest();

        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.put().uri("/api/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 수정 시 403 Forbidden이 발생한다")
    void updatePostWithoutAuthorization() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        PostUpdateRequest request = updateAllFieldsRequest();

        String requestJson = objectMapper.writeValueAsString(request);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.put().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + readerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("작성자가 임시저장된 게시글을 발행할 수 있다")
    void publishPostByAuthor() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("토큰 없이 게시글 발행 요청 시 401 Unauthorized가 발생한다")
    void publishPostWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 발행 시 403 Forbidden이 발생한다")
    void publishPostWithoutAuthorization() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("작성자가 발행된 게시글을 숨김 처리할 수 있다")
    void hidePostByAuthor() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("토큰 없이 게시글 숨김 요청 시 401 Unauthorized가 발생한다")
    void hidePostWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 숨김 시 403 Forbidden이 발생한다")
    void hidePostWithoutAuthorization() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
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

    private Long createAndPublishPost(String token) throws JsonProcessingException, UnsupportedEncodingException {
        PostCreateRequest createRequest = createPostRequest(true);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcTestResult createResult = mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PostCreateResponse.class
        );

        return createResponse.postId();
    }

    private Long createAndDraftPost(String token) throws JsonProcessingException, UnsupportedEncodingException {
        PostCreateRequest createRequest = createPostRequest(false);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcTestResult createResult = mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PostCreateResponse.class
        );

        return createResponse.postId();
    }

    private String createSecondMemberAndGetToken() throws JsonProcessingException, UnsupportedEncodingException {
        MemberRegisterRequest secondMemberRequest = MemberFixture.createSecondMemberRegisterRequest();
        memberRegister.register(secondMemberRequest);

        MemberAuthRequest authRequest = MemberFixture.createSecondMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(authRequest);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                MemberAuthResponse.class
        );

        return authResponse.accessToken();
    }
}