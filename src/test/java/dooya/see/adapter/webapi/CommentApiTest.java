package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.*;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.post.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
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
import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class CommentApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRegister memberRegister;

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        @DisplayName("로그인한 사용자가 게시글에 댓글을 작성할 수 있다")
        @Test
        void createComment() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            CommentCreateRequest request = new CommentCreateRequest("좋은 게시글이네요!");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentCreateResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentCreateResponse.class);

            assertThat(response.commentId()).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.body()).isEqualTo("좋은 게시글이네요!");
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(response.parentCommentId()).isNull();
            assertThat(response.createdAt()).isNotNull();
        }

        @DisplayName("답글을 작성할 수 있다")
        @Test
        void createReply() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long parentCommentId = createCommentHelper(token, postId, "원본 댓글");

            CommentCreateRequest request = new CommentCreateRequest("답글입니다", parentCommentId);
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentCreateResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentCreateResponse.class);

            assertThat(response.commentId()).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.parentCommentId()).isEqualTo(parentCommentId);
            assertThat(response.body()).isEqualTo("답글입니다");
        }

        @DisplayName("토큰 없이 댓글 작성 요청 시 401 Unauthorized가 발생한다")
        @Test
        void createCommentWithoutToken() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            CommentCreateRequest request = new CommentCreateRequest("댓글 내용");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("존재하지 않는 게시글에 댓글을 작성하려고 하면 404 Not Found가 발생한다")
        @Test
        void createCommentOnNonExistentPost() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();

            CommentCreateRequest request = new CommentCreateRequest("댓글 내용");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{postId}/comments", 999L)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class GetComment {

        @DisplayName("게시글의 댓글 목록을 조회할 수 있다")
        @Test
        void getCommentsByPost() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            createCommentHelper(token, postId, "첫 번째 댓글");
            createCommentHelper(token, postId, "두 번째 댓글");

            MvcTestResult result = mvcTester.get().uri("/api/posts/{postId}/comments", postId)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            CommentDetailResponse[] comments = objectMapper.readValue(responseContent, CommentDetailResponse[].class);

            assertThat(comments).hasSize(2);
            assertThat(comments[0].postId()).isEqualTo(postId);
            assertThat(comments[1].postId()).isEqualTo(postId);
            assertThat(comments[0].body()).contains("댓글");
            assertThat(comments[1].body()).contains("댓글");
        }

        @DisplayName("특정 댓글을 조회할 수 있다")
        @Test
        void getComment() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "조회할 댓글");

            MvcTestResult result = mvcTester.get().uri("/api/comments/{commentId}", commentId)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentDetailResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentDetailResponse.class);

            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.body()).isEqualTo("조회할 댓글");
            assertThat(response.isReply()).isFalse();
        }

        @DisplayName("특정 댓글의 답글 목록을 조회할 수 있다")
        @Test
        void getReplies() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long parentCommentId = createCommentHelper(token, postId, "원본 댓글");

            createReply(token, postId, "첫 번째 답글", parentCommentId);
            createReply(token, postId, "두 번째 답글", parentCommentId);

            MvcTestResult result = mvcTester.get().uri("/api/comments/{parentCommentId}/replies", parentCommentId)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            CommentDetailResponse[] replies = objectMapper.readValue(responseContent, CommentDetailResponse[].class);

            assertThat(replies).hasSize(2);
            assertThat(replies[0].parentCommentId()).isEqualTo(parentCommentId);
            assertThat(replies[1].parentCommentId()).isEqualTo(parentCommentId);
            assertThat(replies[0].isReply()).isTrue();
            assertThat(replies[1].isReply()).isTrue();
        }

        @DisplayName("존재하지 않는 댓글을 조회하려고 하면 404 Not Found가 발생한다")
        @Test
        void getNonExistentComment() {
            MvcTestResult result = mvcTester.get().uri("/api/comments/{commentId}", 999L)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원별 댓글 조회")
    class GetCommentsByMember {

        @DisplayName("본인의 댓글 목록을 조회하면 모든 상태의 댓글이 반환된다")
        @Test
        void getMyComments() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            Long memberId = getCurrentMemberId(token);

            createCommentHelper(token, postId, "활성 댓글");
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            // 댓글 삭제
            mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/comments", memberId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            CommentDetailResponse[] comments = objectMapper.readValue(responseContent, CommentDetailResponse[].class);

            assertThat(comments).hasSize(2); // ACTIVE + DELETED 모두 반환
        }

        @DisplayName("다른 사용자의 댓글 목록을 조회하면 활성 상태만 반환된다")
        @Test
        void getOtherUserComments() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);

            Long authorMemberId = getCurrentMemberId(authorToken);

            createCommentHelper(authorToken, postId, "활성 댓글");
            Long commentId = createCommentHelper(authorToken, postId, "삭제할 댓글");

            // 댓글 삭제
            mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + authorToken)
                    .exchange();

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/comments", authorMemberId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            CommentDetailResponse[] comments = objectMapper.readValue(responseContent, CommentDetailResponse[].class);

            assertThat(comments).hasSize(1); // ACTIVE 상태만 반환
            assertThat(comments[0].status()).isEqualTo(CommentStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @DisplayName("작성자가 댓글을 수정할 수 있다")
        @Test
        void updateComment() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "원본 댓글");

            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/comments/{commentId}", commentId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentDetailResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentDetailResponse.class);

            assertThat(response.body()).isEqualTo("수정된 댓글");
            assertThat(response.modifiedAt()).isNotNull();
        }

        @DisplayName("토큰 없이 댓글 수정 요청 시 401 Unauthorized가 발생한다")
        @Test
        void updateCommentWithoutToken() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "원본 댓글");

            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/comments/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("작성자가 아닌 사용자가 댓글 수정 시 403 Forbidden이 발생한다")
        @Test
        void updateCommentWithoutAuthorization() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);
            Long commentId = createCommentHelper(authorToken, postId, "원본 댓글");

            String readerToken = createSecondMemberAndGetToken();

            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/comments/{commentId}", commentId)
                    .header("Authorization", "Bearer " + readerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @DisplayName("작성자가 댓글을 삭제할 수 있다")
        @Test
        void deleteComment() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentDetailResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentDetailResponse.class);

            assertThat(response.status()).isEqualTo(CommentStatus.DELETED);
        }

        @DisplayName("토큰 없이 댓글 삭제 요청 시 401 Unauthorized가 발생한다")
        @Test
        void deleteCommentWithoutToken() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("작성자가 아닌 사용자가 댓글 삭제 시 403 Forbidden이 발생한다")
        @Test
        void deleteCommentWithoutAuthorization() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);
            Long commentId = createCommentHelper(authorToken, postId, "삭제할 댓글");

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @DisplayName("이미 삭제된 댓글을 다시 삭제하려고 하면 409 Conflict가 발생한다")
        @Test
        void deleteCommentAlreadyDeleted() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            // 첫 번째 삭제
            mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            // 두 번째 삭제 시도
            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }
    }

    @Nested
    @DisplayName("댓글 숨김")
    class HideComment {

        @DisplayName("작성자가 댓글을 숨김 처리할 수 있다")
        @Test
        void hideComment() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "숨길 댓글");

            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentDetailResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentDetailResponse.class);

            assertThat(response.status()).isEqualTo(CommentStatus.HIDDEN);
        }

        @DisplayName("토큰 없이 댓글 숨김 요청 시 401 Unauthorized가 발생한다")
        @Test
        void hideCommentWithoutToken() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "숨길 댓글");

            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/hide", commentId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("작성자가 아닌 사용자가 댓글 숨김 시 403 Forbidden이 발생한다")
        @Test
        void hideCommentWithoutAuthorization() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);
            Long commentId = createCommentHelper(authorToken, postId, "숨길 댓글");

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @DisplayName("이미 숨김 처리된 댓글을 다시 숨기려고 하면 409 Conflict가 발생한다")
        @Test
        void hideCommentAlreadyHidden() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "숨길 댓글");

            // 첫 번째 숨김
            mvcTester.post().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            // 두 번째 숨김 시도
            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }

        @DisplayName("삭제된 댓글을 숨기려고 하면 409 Conflict가 발생한다")
        @Test
        void hideDeletedComment() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "댓글");

            // 댓글 삭제
            mvcTester.post().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            // 삭제된 댓글 숨김 시도
            MvcTestResult result = mvcTester.post().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }
    }

    // Helper Methods
    private String createMemberAndGetToken() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

        return authResponse.accessToken();
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
                loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

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
                createResult.getResponse().getContentAsString(), PostCreateResponse.class);

        return createResponse.postId();
    }

    private Long createCommentHelper(String token, Long postId, String content) throws JsonProcessingException, UnsupportedEncodingException {
        CommentCreateRequest request = new CommentCreateRequest(content);
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        CommentCreateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentCreateResponse.class);

        return response.commentId();
    }

    private Long createReply(String token, Long postId, String content, Long parentCommentId) throws JsonProcessingException, UnsupportedEncodingException {
        CommentCreateRequest request = new CommentCreateRequest(content, parentCommentId);
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        CommentCreateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentCreateResponse.class);

        return response.commentId();
    }

    private Long getCurrentMemberId(String token) throws JsonProcessingException, UnsupportedEncodingException {
        MvcTestResult result = mvcTester.get().uri("/api/members/my")
                .header("Authorization", "Bearer " + token)
                .exchange();

        MemberProfileResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), MemberProfileResponse.class);

        return response.memberId();
    }
}
