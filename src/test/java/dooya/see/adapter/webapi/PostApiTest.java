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
import dooya.see.domain.post.*;
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
import static org.assertj.core.api.Assertions.*;
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
    @DisplayName("비로그인 사용자가 공개되지 않은 게시글 조회 시 403 Forbidden이 발생한다")
    void getUnpublishedPostWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long draftPostId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", draftPostId)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("로그인한 사용자가 다른 사용자의 비공개 게시글 조회 시 403 Forbidden이 발생한다")
    void getOtherUserUnpublishedPost() throws UnsupportedEncodingException, JsonProcessingException {
        String authorToken = createMemberAndGetToken();
        Long draftPostId = createAndDraftPost(authorToken);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", draftPostId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("작성자는 본인의 비공개 게시글을 조회할 수 있다")
    void getMyUnpublishedPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long draftPostId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", draftPostId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.postId()).isEqualTo(draftPostId);
        assertThat(response.status()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    @DisplayName("비로그인 사용자가 숨김 처리된 게시글 조회 시 403 Forbidden이 발생한다")
    void getHiddenPostWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        // 게시글을 숨김 처리
        mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("작성자는 본인의 숨김 처리된 게시글을 조회할 수 있다")
    void getMyHiddenPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        // 게시글을 숨김 처리
        mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("다른 사용자가 숨김 처리된 게시글 조회 시 403 Forbidden이 발생한다")
    void getOtherUserHiddenPost() throws UnsupportedEncodingException, JsonProcessingException {
        String authorToken = createMemberAndGetToken();
        Long postId = createAndPublishPost(authorToken);

        // 게시글을 숨김 처리
        mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + authorToken)
                .exchange();

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
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
        assertThat(response.publishedAt()).isNotNull();
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
    @DisplayName("이미 발행된 게시글을 다시 발행하려고 하면 409 Conflict가 발생한다")
    void publishPostAlreadyPublished() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.CONFLICT);
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

    @Test
    @DisplayName("이미 숨김 처리된 게시글을 다시 숨기려고 하면 409 Conflict가 발생한다")
    void hidePostAlreadyHidden() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 임시저장 게시글 숨김 시 403 Forbidden이 발생한다")
    void hideDraftPostWithoutAuthorization() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("작성자가 임시저장 게시글을 숨김 처리할 수 있다")
    void hideDraftPostByAuthor() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("숨김 상태의 게시글을 다시 발행할 수 있다")
    void publishPostFromHiddenStatus() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("작성자가 게시글을 삭제할 수 있다")
    void deletePostByAuthor() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("토큰 없이 게시글 삭제 요청 시 401 Unauthorized가 발생한다")
    void deletePostWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 삭제 시 403 Forbidden이 발생한다")
    void deletePostWithoutAuthorization() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 다시 삭제하려고 하면 409 Conflict가 발생한다")
    void deletePostAlreadyDeleted() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 삭제하려고 하면 404 Not Found가 발생한다")
    void deleteNonExistentPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", 999L)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("임시저장 상태의 게시글을 삭제할 수 있다")
    void deleteDraftPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("숨김 상태의 게시글을 삭제할 수 있다")
    void deleteHiddenPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.status()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("발행된 게시글에 좋아요를 누르면 좋아요 수가 1 증가한다")
    void likePublishedPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/like", postId)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글에 좋아요를 누르면 좋아요 수는 변경되지 않는다")
    void likeDraftPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/like", postId)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.likeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("발행된 게시글에 댓글을 추가하면 댓글 수가 1 증가한다")
    void addCommentToPublishedPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/comment", postId)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.commentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글에 댓글을 추가하면 댓글 수는 변경되지 않는다")
    void addCommentToDraftPost() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndDraftPost(token);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/comment", postId)
                .exchange();

        assertThat(result).hasStatusOk();

        PostDetailResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

        assertThat(response.commentCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("공개된 게시글 목록을 조회할 수 있다")
    void getPublicPosts() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);
        createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult result = mvcTester.get().uri("/api/posts")
                .exchange();

        assertThat(result).hasStatusOk();

        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

        assertThat(posts).hasSize(2);
        assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(posts[1].status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("특정 카테고리의 공개 게시글 목록을 조회할 수 있다")
    void getPostsByCategory() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token); // TECH 카테고리

        MvcTestResult result = mvcTester.get().uri("/api/posts/category/TECH")
                .exchange();

        assertThat(result).hasStatusOk();

        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

        assertThat(posts).hasSize(1);
        assertThat(posts[0].category()).isEqualTo(PostCategory.TECH);
        assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("본인의 게시글 목록을 조회하면 모든 상태의 게시글이 반환된다")
    void getMyPosts() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();

        Long publishedPostId = createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId)
                .exchange();
        PostDetailResponse postDetail = objectMapper.readValue(
                postResult.getResponse().getContentAsString(), PostDetailResponse.class);
        Long actualMemberId = postDetail.authorId();

        MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/posts", actualMemberId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        assertThat(result).hasStatusOk();

        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

        assertThat(posts).hasSize(2);
    }

    @Test
    @DisplayName("다른 사용자의 게시글 목록을 조회하면 공개된 게시글만 반환된다")
    void getOtherUserPosts() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long publishedPostId = createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId)
                .exchange();
        PostDetailResponse postDetail = objectMapper.readValue(
                postResult.getResponse().getContentAsString(), PostDetailResponse.class);
        Long authorMemberId = postDetail.authorId();

        String readerToken = createSecondMemberAndGetToken();

        MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/posts", authorMemberId)
                .header("Authorization", "Bearer " + readerToken)
                .exchange();

        assertThat(result).hasStatusOk();

        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

        assertThat(posts).hasSize(1); // PUBLISHED 게시글만 조회
        assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("비로그인 상태로 다른 사용자 게시글을 조회하면 공개된 게시글만 반환된다")
    void getOtherUserPostsWithoutToken() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long publishedPostId = createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId)
                .exchange();
        PostDetailResponse postDetail = objectMapper.readValue(
                postResult.getResponse().getContentAsString(), PostDetailResponse.class);
        Long authorMemberId = postDetail.authorId();

        MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/posts", authorMemberId)
                .exchange();

        assertThat(result).hasStatusOk();

        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

        assertThat(posts).hasSize(1);
        assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("카테고리로 게시글을 검색할 수 있다")
    void searchPostsByCategory() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?category=TECH")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(2); // TECH 카테고리 게시글 2개
        assertThat(posts).allMatch(post -> post.category() == PostCategory.TECH);
    }

    @Test
    @DisplayName("키워드로 게시글을 검색할 수 있다")
    void searchPostsByKeyword() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token); // "테스트 게시글 제목입니다" 포함

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?keyword=테스트")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(1);
        assertThat(posts[0].title()).contains("테스트");
    }

    @Test
    @DisplayName("제목 키워드로 게시글을 검색할 수 있다")
    void searchPostsByTitleKeyword() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?titleKeyword=게시글")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(1);
        assertThat(posts[0].title()).contains("게시글");
    }

    @Test
    @DisplayName("내용 키워드로 게시글을 검색할 수 있다")
    void searchPostsByContentKeyword() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token); // "테스트 게시글 내용입니다" 포함

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?contentKeyword=내용")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(1);
        assertThat(posts[0].body()).contains("내용");
    }

    @Test
    @DisplayName("특정 회원이 작성한 게시글을 검색할 수 있다")
    void searchPostsByMemberId() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        Long postId = createAndPublishPost(token);
        
        // 회원 ID 얻기
        MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", postId).exchange();
        PostDetailResponse postDetail = objectMapper.readValue(
                postResult.getResponse().getContentAsString(), PostDetailResponse.class);
        Long memberId = postDetail.authorId();

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?memberId=" + memberId)
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(1);
        assertThat(posts[0].authorId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("게시글 상태로 검색할 수 있다")
    void searchPostsByStatus() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?status=PUBLISHED")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(1);
        assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("여러 조건을 조합하여 게시글을 검색할 수 있다")
    void searchPostsByMultipleConditions() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?category=TECH&status=PUBLISHED&keyword=테스트")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(1);
        assertThat(posts[0].category()).isEqualTo(PostCategory.TECH);
        assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(posts[0].title()).contains("테스트");
    }

    @Test
    @DisplayName("검색 조건에 맞는 게시글이 없으면 빈 배열을 반환한다")
    void searchPostsWithNoResults() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search?keyword=존재하지않는키워드")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("검색 조건 없이 요청하면 모든 게시글을 반환한다")
    void searchPostsWithoutConditions() throws UnsupportedEncodingException, JsonProcessingException {
        String token = createMemberAndGetToken();
        createAndPublishPost(token);
        createAndDraftPost(token);

        MvcTestResult result = mvcTester.get()
                .uri("/api/posts/search")
                .exchange();

        assertThat(result).hasStatusOk();
        
        String responseContent = result.getResponse().getContentAsString();
        PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);
        
        assertThat(posts).hasSize(2); // 모든 게시글 반환
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