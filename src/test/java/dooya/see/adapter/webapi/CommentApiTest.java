package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.*;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.post.CommentCreateRequest;
import dooya.see.domain.post.CommentStatus;
import dooya.see.domain.post.CommentUpdateRequest;
import dooya.see.domain.post.PostCreateRequest;
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

import static dooya.see.domain.member.MemberFixture.createMemberAuthRequest;
import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.post.PostFixture.createPostRequest;
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
    class 댓글_생성 {
        @Test
        void 로그인한_사용자가_게시글에_댓글을_작성할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 답글을_작성할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 토큰_없이_댓글_작성_요청_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 존재하지_않는_게시글에_댓글을_작성하려고_하면_404_Not_Found가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
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
    class 댓글_조회 {
        @Test
        void 게시글의_댓글_목록을_조회할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 특정_댓글을_조회할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 특정_댓글의_답글_목록을_조회할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 존재하지_않는_댓글을_조회하려고_하면_404_Not_Found가_발생한다() {
            MvcTestResult result = mvcTester.get().uri("/api/comments/{commentId}", 999L)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class 회원별_댓글_조회 {
        @Test
        void 본인의_댓글_목록을_조회하면_모든_상태의_댓글이_반환된다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            Long memberId = extractCurrentMemberId(token);

            createCommentHelper(token, postId, "활성 댓글");
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            // 댓글 삭제
            mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
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

        @Test
        void 다른_사용자의_댓글_목록을_조회하면_활성_상태만_반환된다() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);

            Long authorMemberId = extractCurrentMemberId(authorToken);

            createCommentHelper(authorToken, postId, "활성 댓글");
            Long commentId = createCommentHelper(authorToken, postId, "삭제할 댓글");

            // 댓글 삭제
            mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
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
    class 댓글_수정 {
        @Test
        void 작성자가_댓글을_수정할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 토큰_없이_댓글_수정_요청_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
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

        @Test
        void 작성자가_아닌_사용자가_댓글_수정_시_403_Forbidden이_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
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
    class 댓글_삭제 {
        @Test
        void 작성자가_댓글을_삭제할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            MvcTestResult result = mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentDetailResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentDetailResponse.class);

            assertThat(response.status()).isEqualTo(CommentStatus.DELETED);
        }

        @Test
        void 토큰_없이_댓글_삭제_요청_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            MvcTestResult result = mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_댓글_삭제_시_403_Forbidden이_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);
            Long commentId = createCommentHelper(authorToken, postId, "삭제할 댓글");

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_삭제된_댓글을_다시_삭제하려고_하면_409_Conflict가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "삭제할 댓글");

            // 첫 번째 삭제
            mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            // 두 번째 삭제 시도
            MvcTestResult result = mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class 댓글_숨김 {
        @Test
        void 작성자가_댓글을_숨김_처리할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "숨길 댓글");

            MvcTestResult result = mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            CommentDetailResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), CommentDetailResponse.class);

            assertThat(response.status()).isEqualTo(CommentStatus.HIDDEN);
        }

        @Test
        void 토큰_없이_댓글_숨김_요청_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "숨길 댓글");

            MvcTestResult result = mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_댓글_숨김_시_403_Forbidden이_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);
            Long commentId = createCommentHelper(authorToken, postId, "숨길 댓글");

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_숨김_처리된_댓글을_다시_숨기려고_하면_409_Conflict가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "숨길 댓글");

            // 첫 번째 숨김
            mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            // 두 번째 숨김 시도
            MvcTestResult result = mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void 삭제된_댓글을_숨기려고_하면_409_Conflict가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);
            Long commentId = createCommentHelper(token, postId, "댓글");

            // 댓글 삭제
            mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            // 삭제된 댓글 숨김 시도
            MvcTestResult result = mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
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

    private Long extractCurrentMemberId(String token) throws JsonProcessingException, UnsupportedEncodingException {
        MvcTestResult result = mvcTester.get().uri("/api/members/my")
                .header("Authorization", "Bearer " + token)
                .exchange();

        MemberProfileResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), MemberProfileResponse.class);

        return response.memberId();
    }
}
