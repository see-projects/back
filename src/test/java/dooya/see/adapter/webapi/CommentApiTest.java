package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.*;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.dto.MemberAuthRequest;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.dto.MemberRegisterRequest;
import dooya.see.domain.post.dto.CommentCreateRequest;
import dooya.see.domain.post.CommentStatus;
import dooya.see.domain.post.dto.CommentUpdateRequest;
import dooya.see.domain.post.dto.PostCreateRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class CommentApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRegister memberRegister;

    private static final String VALID_TOKEN_PREFIX = "Bearer ";
    private static final String SAMPLE_COMMENT_TEXT = "좋은 게시글이네요!";
    private static final String UPDATED_COMMENT_TEXT = "수정된 댓글";
    private static final String PARENT_COMMENT_TEXT = "원본 댓글";
    private static final String REPLY_TEXT = "답글입니다";
    private static final Long NON_EXISTENT_ID = 999L;

    private String authorToken;
    private String readerToken;

    @BeforeEach
    void setUp() throws JsonProcessingException, UnsupportedEncodingException {
        authorToken = createMemberAndGetToken();
        readerToken = createSecondMemberAndGetToken();
    }

    @Nested
    class 댓글_생성 {
        @Test
        void 로그인한_사용자가_게시글에_댓글을_작성할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            CommentCreateRequest request = new CommentCreateRequest(SAMPLE_COMMENT_TEXT);

            MvcTestResult result = performCommentCreate(postId, request, authorToken);

            assertThatCommentCreated(result, postId, SAMPLE_COMMENT_TEXT);
        }

        @Test
        void 답글을_작성할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long parentCommentId = createTestComment(postId, PARENT_COMMENT_TEXT);
            CommentCreateRequest request = new CommentCreateRequest(REPLY_TEXT, parentCommentId);

            MvcTestResult result = performCommentCreate(postId, request, authorToken);

            assertThatReplyCreated(result, postId, parentCommentId, REPLY_TEXT);
        }

        @Test
        void 토큰_없이_댓글_작성_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            CommentCreateRequest request = new CommentCreateRequest("댓글 내용");

            MvcTestResult result = performCommentCreateWithoutAuth(postId, request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 존재하지_않는_게시글에_댓글_작성_시_404_Not_Found가_발생한다() throws JsonProcessingException {
            CommentCreateRequest request = new CommentCreateRequest("댓글 내용");

            MvcTestResult result = performCommentCreate(NON_EXISTENT_ID, request, authorToken);

            assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        }

        private void assertThatCommentCreated(MvcTestResult result, Long expectedPostId, String expectedContent)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentCreateResponse response = parseResponse(result, CommentCreateResponse.class);
            assertThat(response.commentId()).isNotNull();
            assertThat(response.postId()).isEqualTo(expectedPostId);
            assertThat(response.body()).isEqualTo(expectedContent);
            assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(response.parentCommentId()).isNull();
            assertThat(response.createdAt()).isNotNull();
        }

        private void assertThatReplyCreated(MvcTestResult result, Long expectedPostId, Long expectedParentId, String expectedContent)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentCreateResponse response = parseResponse(result, CommentCreateResponse.class);
            assertThat(response.commentId()).isNotNull();
            assertThat(response.postId()).isEqualTo(expectedPostId);
            assertThat(response.parentCommentId()).isEqualTo(expectedParentId);
            assertThat(response.body()).isEqualTo(expectedContent);
        }
    }

    @Nested
    class 댓글_조회 {
        @Test
        void 게시글의_댓글_목록을_조회할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            createTestComment(postId, "첫 번째 댓글");
            createTestComment(postId, "두 번째 댓글");

            MvcTestResult result = performPostCommentsList(postId);

            assertThatPostCommentsListed(result, postId, 2);
        }

        @Test
        void 특정_댓글을_조회할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "조회할 댓글");

            MvcTestResult result = performCommentGet(commentId);

            assertThatCommentRetrieved(result, commentId, postId, "조회할 댓글");
        }

        @Test
        void 특정_댓글의_답글_목록을_조회할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long parentCommentId = createTestComment(postId, PARENT_COMMENT_TEXT);
            createTestReply(postId, parentCommentId, "첫 번째 답글");
            createTestReply(postId, parentCommentId, "두 번째 답글");

            MvcTestResult result = performCommentRepliesList(parentCommentId);

            assertThatCommentRepliesListed(result, parentCommentId, 2);
        }

        @Test
        void 존재하지_않는_댓글_조회_시_404_Not_Found가_발생한다() {
            MvcTestResult result = performCommentGet(NON_EXISTENT_ID);

            assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        }

        private void assertThatPostCommentsListed(MvcTestResult result, Long expectedPostId, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse[] comments = parseResponseArray(result, CommentDetailResponse[].class);
            assertThat(comments).hasSize(expectedSize);
            assertThat(comments).allMatch(comment -> comment.postId().equals(expectedPostId));
            assertThat(comments).allMatch(comment -> comment.body().contains("댓글"));
        }

        private void assertThatCommentRetrieved(MvcTestResult result, Long expectedCommentId, Long expectedPostId, String expectedContent)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse response = parseResponse(result, CommentDetailResponse.class);
            assertThat(response.commentId()).isEqualTo(expectedCommentId);
            assertThat(response.postId()).isEqualTo(expectedPostId);
            assertThat(response.body()).isEqualTo(expectedContent);
            assertThat(response.isReply()).isFalse();
        }

        private void assertThatCommentRepliesListed(MvcTestResult result, Long expectedParentId, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse[] replies = parseResponseArray(result, CommentDetailResponse[].class);
            assertThat(replies).hasSize(expectedSize);
            assertThat(replies).allMatch(reply -> reply.parentCommentId().equals(expectedParentId));
            assertThat(replies).allMatch(CommentDetailResponse::isReply);
        }
    }

    @Nested
    class 회원별_댓글_조회 {
        @Test
        void 본인의_댓글_목록_조회_시_모든_상태가_반환된다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long memberId = getCurrentMemberId(authorToken);

            createTestComment(postId, "활성 댓글");
            Long commentId = createTestComment(postId, "삭제할 댓글");
            performCommentDelete(commentId, authorToken);

            MvcTestResult result = performMemberCommentsList(memberId, authorToken);

            assertThatMemberCommentsListed(result, 2);
        }

        @Test
        void 다른_사용자의_댓글_목록_조회_시_활성_상태만_반환된다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long authorMemberId = getCurrentMemberId(authorToken);

            createTestComment(postId, "활성 댓글");
            Long commentId = createTestComment(postId, "삭제할 댓글");
            performCommentDelete(commentId, authorToken);

            MvcTestResult result = performMemberCommentsList(authorMemberId, readerToken);

            assertThatPublicMemberCommentsListed(result, 1);
        }

        private void assertThatMemberCommentsListed(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse[] comments = parseResponseArray(result, CommentDetailResponse[].class);
            assertThat(comments).hasSize(expectedSize);
        }

        private void assertThatPublicMemberCommentsListed(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse[] comments = parseResponseArray(result, CommentDetailResponse[].class);
            assertThat(comments).hasSize(expectedSize);
            assertThat(comments).allMatch(comment -> comment.status() == CommentStatus.ACTIVE);
        }
    }

    @Nested
    class 댓글_수정 {
        @Test
        void 작성자가_댓글을_수정할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, PARENT_COMMENT_TEXT);
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_COMMENT_TEXT);

            MvcTestResult result = performCommentUpdate(commentId, request, authorToken);

            assertThatCommentUpdated(result, UPDATED_COMMENT_TEXT);
        }

        @Test
        void 토큰_없이_댓글_수정_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, PARENT_COMMENT_TEXT);
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_COMMENT_TEXT);

            MvcTestResult result = performCommentUpdateWithoutAuth(commentId, request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_댓글_수정_시_403_Forbidden이_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, PARENT_COMMENT_TEXT);
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_COMMENT_TEXT);

            MvcTestResult result = performCommentUpdate(commentId, request, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        private void assertThatCommentUpdated(MvcTestResult result, String expectedContent)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse response = parseResponse(result, CommentDetailResponse.class);
            assertThat(response.body()).isEqualTo(expectedContent);
            assertThat(response.modifiedAt()).isNotNull();
        }
    }

    @Nested
    class 댓글_삭제 {
        @Test
        void 작성자가_댓글을_삭제할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "삭제할 댓글");

            MvcTestResult result = performCommentDelete(commentId, authorToken);

            assertThatCommentDeleted(result);
        }

        @Test
        void 토큰_없이_댓글_삭제_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "삭제할 댓글");

            MvcTestResult result = performCommentDeleteWithoutAuth(commentId);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_댓글_삭제_시_403_Forbidden이_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "삭제할 댓글");

            MvcTestResult result = performCommentDelete(commentId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_삭제된_댓글_재삭제_시_409_Conflict가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "삭제할 댓글");
            performCommentDelete(commentId, authorToken);

            MvcTestResult result = performCommentDelete(commentId, authorToken);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        private void assertThatCommentDeleted(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse response = parseResponse(result, CommentDetailResponse.class);
            assertThat(response.status()).isEqualTo(CommentStatus.DELETED);
        }
    }

    @Nested
    class 댓글_숨김 {
        @Test
        void 작성자가_댓글을_숨김_처리할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "숨길 댓글");

            MvcTestResult result = performCommentHide(commentId, authorToken);

            assertThatCommentHidden(result);
        }

        @Test
        void 토큰_없이_댓글_숨김_시_401_Unauthorized가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "숨길 댓글");

            MvcTestResult result = performCommentHideWithoutAuth(commentId);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_댓글_숨김_시_403_Forbidden이_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "숨길 댓글");

            MvcTestResult result = performCommentHide(commentId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_숨김_처리된_댓글_재숨김_시_409_Conflict가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "숨길 댓글");
            performCommentHide(commentId, authorToken);

            MvcTestResult result = performCommentHide(commentId, authorToken);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void 삭제된_댓글_숨김_시_409_Conflict가_발생한다() throws JsonProcessingException, UnsupportedEncodingException {
            Long postId = createTestPublishedPost();
            Long commentId = createTestComment(postId, "댓글");
            performCommentDelete(commentId, authorToken);

            MvcTestResult result = performCommentHide(commentId, authorToken);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        private void assertThatCommentHidden(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            CommentDetailResponse response = parseResponse(result, CommentDetailResponse.class);
            assertThat(response.status()).isEqualTo(CommentStatus.HIDDEN);
        }
    }

    // 헬퍼 메서드들
    private String createMemberAndGetToken() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse = parseResponse(loginResult, MemberAuthResponse.class);
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

        MemberAuthResponse authResponse = parseResponse(loginResult, MemberAuthResponse.class);
        return authResponse.accessToken();
    }

    private Long createTestPublishedPost() throws JsonProcessingException, UnsupportedEncodingException {
        PostCreateRequest createRequest = createPostRequest(true);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcTestResult createResult = mvcTester.post().uri("/api/posts")
                .header("Authorization", VALID_TOKEN_PREFIX + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = parseResponse(createResult, PostCreateResponse.class);
        return createResponse.postId();
    }

    private Long createTestComment(Long postId, String content) throws JsonProcessingException, UnsupportedEncodingException {
        CommentCreateRequest request = new CommentCreateRequest(content);
        MvcTestResult result = performCommentCreate(postId, request, authorToken);
        CommentCreateResponse response = parseResponse(result, CommentCreateResponse.class);
        return response.commentId();
    }

    private Long createTestReply(Long postId, Long parentCommentId, String content) throws JsonProcessingException, UnsupportedEncodingException {
        CommentCreateRequest request = new CommentCreateRequest(content, parentCommentId);
        MvcTestResult result = performCommentCreate(postId, request, authorToken);
        CommentCreateResponse response = parseResponse(result, CommentCreateResponse.class);
        return response.commentId();
    }

    private Long getCurrentMemberId(String token) throws JsonProcessingException, UnsupportedEncodingException {
        MvcTestResult result = mvcTester.get().uri("/api/members/me")
                .header("Authorization", VALID_TOKEN_PREFIX + token)
                .exchange();

        MemberProfileResponse response = parseResponse(result, MemberProfileResponse.class);
        return response.memberId();
    }

    // API 호출 헬퍼 메서드들
    private MvcTestResult performCommentCreate(Long postId, CommentCreateRequest request, String token)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                .header("Authorization", VALID_TOKEN_PREFIX + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performCommentCreateWithoutAuth(Long postId, CommentCreateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.post().uri("/api/posts/{postId}/comments", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performPostCommentsList(Long postId) {
        return mvcTester.get().uri("/api/posts/{postId}/comments", postId).exchange();
    }

    private MvcTestResult performCommentGet(Long commentId) {
        return mvcTester.get().uri("/api/comments/{commentId}", commentId).exchange();
    }

    private MvcTestResult performCommentRepliesList(Long parentCommentId) {
        return mvcTester.get().uri("/api/comments/{parentCommentId}/replies", parentCommentId).exchange();
    }

    private MvcTestResult performMemberCommentsList(Long memberId, String token) {
        return mvcTester.get().uri("/api/members/{memberId}/comments", memberId)
                .header("Authorization", VALID_TOKEN_PREFIX + token)
                .exchange();
    }

    private MvcTestResult performCommentUpdate(Long commentId, CommentUpdateRequest request, String token)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/comments/{commentId}", commentId)
                .header("Authorization", VALID_TOKEN_PREFIX + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performCommentUpdateWithoutAuth(Long commentId, CommentUpdateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();
    }

    private MvcTestResult performCommentDelete(Long commentId, String token) {
        return mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId)
                .header("Authorization", VALID_TOKEN_PREFIX + token)
                .exchange();
    }

    private MvcTestResult performCommentDeleteWithoutAuth(Long commentId) {
        return mvcTester.delete().uri("/api/comments/{commentId}/delete", commentId).exchange();
    }

    private MvcTestResult performCommentHide(Long commentId, String token) {
        return mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId)
                .header("Authorization", VALID_TOKEN_PREFIX + token)
                .exchange();
    }

    private MvcTestResult performCommentHideWithoutAuth(Long commentId) {
        return mvcTester.patch().uri("/api/comments/{commentId}/hide", commentId).exchange();
    }

    // 응답 파싱 헬퍼 메서드들
    private <T> T parseResponse(MvcTestResult result, Class<T> responseType)
            throws UnsupportedEncodingException, JsonProcessingException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }

    private <T> T parseResponseArray(MvcTestResult result, Class<T> responseType)
            throws UnsupportedEncodingException, JsonProcessingException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }
}