package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.post.*;
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
import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class PostApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRegister memberRegister;

    private String authorToken;
    private String readerToken;

    @BeforeEach
    void setUp() throws JsonProcessingException, UnsupportedEncodingException {
        authorToken = createMemberAndGetToken();
        readerToken = createSecondMemberAndGetToken();
    }

    @Nested
    class 게시글_생성 {
        @Test
        void 로그인한_사용자가_게시글을_생성할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            PostCreateRequest request = createPostRequest(true);

            MvcTestResult result = performPostCreate(request, authorToken);

            assertThatPostCreated(result, request);
        }

        @Test
        void 토큰_없이_게시글_생성_시_401_Unauthorized가_발생한다() throws JsonProcessingException {
            PostCreateRequest request = createPostRequest(true);

            MvcTestResult result = performPostCreateWithoutToken(request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        private void assertThatPostCreated(MvcTestResult result, PostCreateRequest request)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostCreateResponse response = parseResponse(result, PostCreateResponse.class);
            assertThat(response.postId()).isNotNull();
            assertThat(response.title()).isEqualTo(request.title());
            assertThat(response.category()).isEqualTo(request.category());
            assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(response.createdAt()).isNotNull();
        }
    }

    @Nested
    class 게시글_조회 {
        @Test
        void 게시글_조회_시_조회수가_증가한다() throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostGet(postId);

            assertThatPostRetrieved(result, postId);
        }

        @Test
        void 비로그인_사용자가_비공개_게시글_조회_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long draftPostId = createTestDraftPost();

            MvcTestResult result = performPostGet(draftPostId);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 로그인한_사용자가_다른_사용자의_비공개_게시글_조회_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long draftPostId = createTestDraftPost();

            MvcTestResult result = performPostGet(draftPostId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 작성자는_본인의_비공개_게시글을_조회할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long draftPostId = createTestDraftPost();

            MvcTestResult result = performPostGet(draftPostId, authorToken);

            assertThatDraftPostRetrieved(result, draftPostId);
        }

        @Test
        void 비로그인_사용자가_숨김_게시글_조회_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostHide(postId, authorToken);

            MvcTestResult result = performPostGet(postId);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 작성자는_본인의_숨김_게시글을_조회할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostHide(postId, authorToken);

            MvcTestResult result = performPostGet(postId, authorToken);

            assertThatHiddenPostRetrieved(result, postId);
        }

        @Test
        void 다른_사용자가_숨김_게시글_조회_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostHide(postId, authorToken);

            MvcTestResult result = performPostGet(postId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 다른_사용자가_공개_게시글을_조회할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostGet(postId, readerToken);

            assertThatPostRetrieved(result, postId);
        }

        private void assertThatPostRetrieved(MvcTestResult result, Long expectedPostId)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.postId()).isEqualTo(expectedPostId);
        }

        private void assertThatDraftPostRetrieved(MvcTestResult result, Long expectedPostId)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.postId()).isEqualTo(expectedPostId);
            assertThat(response.status()).isEqualTo(PostStatus.DRAFT);
        }

        private void assertThatHiddenPostRetrieved(MvcTestResult result, Long expectedPostId)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.postId()).isEqualTo(expectedPostId);
            assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
        }
    }

    @Nested
    class 게시글_수정 {
        @Test
        void 작성자가_게시글을_수정할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            PostUpdateRequest request = updateAllFieldsRequest();

            MvcTestResult result = performPostUpdate(postId, request, authorToken);

            assertThatPostUpdated(result);
        }

        @Test
        void 토큰_없이_게시글_수정_시_401_Unauthorized가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            PostUpdateRequest request = updateAllFieldsRequest();

            MvcTestResult result = performPostUpdateWithoutToken(postId, request);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_수정_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            PostUpdateRequest request = updateAllFieldsRequest();

            MvcTestResult result = performPostUpdate(postId, request, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        private void assertThatPostUpdated(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.body()).isEqualTo("수정된 내용");
        }
    }

    @Nested
    class 게시글_발행 {
        @Test
        void 작성자가_임시저장_게시글을_발행할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestDraftPost();

            MvcTestResult result = performPostPublish(postId, authorToken);

            assertThatPostPublished(result);
        }

        @Test
        void 토큰_없이_게시글_발행_시_401_Unauthorized가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestDraftPost();

            MvcTestResult result = performPostPublishWithoutToken(postId);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_발행_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestDraftPost();

            MvcTestResult result = performPostPublish(postId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_발행된_게시글_재발행_시_409_Conflict가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostPublish(postId, authorToken);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void 숨김_상태의_게시글을_다시_발행할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostHide(postId, authorToken);

            MvcTestResult result = performPostPublish(postId, authorToken);

            assertThatPostPublished(result);
        }

        private void assertThatPostPublished(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(response.publishedAt()).isNotNull();
        }
    }

    @Nested
    class 게시글_숨김 {
        @Test
        void 작성자가_발행된_게시글을_숨김_처리할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostHide(postId, authorToken);

            assertThatPostHidden(result);
        }

        @Test
        void 작성자가_임시저장_게시글을_숨김_처리할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestDraftPost();

            MvcTestResult result = performPostHide(postId, authorToken);

            assertThatPostHidden(result);
        }

        @Test
        void 토큰_없이_게시글_숨김_시_401_Unauthorized가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostHideWithoutToken(postId);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_숨김_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostHide(postId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_숨김_처리된_게시글_재숨김_시_409_Conflict가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostHide(postId, authorToken);

            MvcTestResult result = performPostHide(postId, authorToken);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        private void assertThatPostHidden(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
        }
    }

    @Nested
    class 게시글_삭제 {
        @Test
        void 작성자가_게시글을_삭제할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostDelete(postId, authorToken);

            assertThatPostDeleted(result);
        }

        @Test
        void 임시저장_게시글을_삭제할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestDraftPost();

            MvcTestResult result = performPostDelete(postId, authorToken);

            assertThatPostDeleted(result);
        }

        @Test
        void 숨김_게시글을_삭제할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostHide(postId, authorToken);

            MvcTestResult result = performPostDelete(postId, authorToken);

            assertThatPostDeleted(result);
        }

        @Test
        void 토큰_없이_게시글_삭제_시_401_Unauthorized가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostDeleteWithoutToken(postId);

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_삭제_시_403_Forbidden이_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            MvcTestResult result = performPostDelete(postId, readerToken);

            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_삭제된_게시글_재삭제_시_409_Conflict가_발생한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();
            performPostDelete(postId, authorToken);

            MvcTestResult result = performPostDelete(postId, authorToken);

            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void 존재하지_않는_게시글_삭제_시_404_Not_Found가_발생한다() {
            MvcTestResult result = performPostDelete(999L, authorToken);

            assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        }

        private void assertThatPostDeleted(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse response = parseResponse(result, PostDetailResponse.class);
            assertThat(response.status()).isEqualTo(PostStatus.DELETED);
        }
    }

    @Nested
    class 게시글_목록_조회 {
        @Test
        void 공개된_게시글_목록을_조회할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();
            createTestPublishedPost();
            createTestDraftPost();

            MvcTestResult result = performPostList();

            assertThatPublicPostsListed(result, 2);
        }

        @Test
        void 특정_카테고리의_공개_게시글_목록을_조회할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();

            MvcTestResult result = performPostListByCategory(PostCategory.TECH);

            assertThatCategoryPostsListed(result, PostCategory.TECH, 1);
        }

        @Test
        void 본인의_게시글_목록_조회_시_모든_상태가_반환된다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long publishedPostId = createTestPublishedPost();
            createTestDraftPost();

            Long memberId = getMemberIdFromPost(publishedPostId);
            MvcTestResult result = performMemberPostList(memberId, authorToken);

            assertThatMemberPostsListed(result, 2);
        }

        @Test
        void 다른_사용자의_게시글_목록_조회_시_공개된_게시글만_반환된다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long publishedPostId = createTestPublishedPost();
            createTestDraftPost();

            Long memberId = getMemberIdFromPost(publishedPostId);
            MvcTestResult result = performMemberPostList(memberId, readerToken);

            assertThatPublicMemberPostsListed(result, 1);
        }

        @Test
        void 비로그인_상태로_사용자_게시글_조회_시_공개된_게시글만_반환된다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long publishedPostId = createTestPublishedPost();
            createTestDraftPost();

            Long memberId = getMemberIdFromPost(publishedPostId);
            MvcTestResult result = performMemberPostListWithoutAuth(memberId);

            assertThatPublicMemberPostsListed(result, 1);
        }

        private void assertThatPublicPostsListed(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.status() == PostStatus.PUBLISHED);
        }

        private void assertThatCategoryPostsListed(MvcTestResult result, PostCategory expectedCategory, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.category() == expectedCategory);
            assertThat(posts).allMatch(post -> post.status() == PostStatus.PUBLISHED);
        }

        private void assertThatMemberPostsListed(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
        }

        private void assertThatPublicMemberPostsListed(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.status() == PostStatus.PUBLISHED);
        }
    }

    @Nested
    class 게시글_검색 {
        @Test
        void 비로그인_사용자가_카테고리로_검색_시_공개된_게시글만_반환된다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();
            createTestDraftPost();

            MvcTestResult result = performPostSearch("category=TECH");

            assertThatPublicSearchResults(result, PostCategory.TECH, 1);
        }

        @Test
        void 로그인한_사용자가_본인_게시글을_카테고리로_검색_시_모든_상태가_반환된다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long publishedPostId = createTestPublishedPost();
            createTestDraftPost();

            Long memberId = getMemberIdFromPost(publishedPostId);
            MvcTestResult result = performPostSearchWithAuth("category=TECH&memberId=" + memberId, authorToken);

            assertThatOwnerSearchResults(result, PostCategory.TECH, memberId, 2);
        }

        @Test
        void 키워드로_게시글을_검색할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();

            MvcTestResult result = performPostSearch("keyword=테스트");

            assertThatKeywordSearchResults(result, "테스트", 1);
        }

        @Test
        void 제목_키워드로_게시글을_검색할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();

            MvcTestResult result = performPostSearch("titleKeyword=게시글");

            assertThatTitleKeywordSearchResults(result, "게시글", 1);
        }

        @Test
        void 내용_키워드로_게시글을_검색할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();

            MvcTestResult result = performPostSearch("contentKeyword=내용");

            assertThatContentKeywordSearchResults(result, "내용", 1);
        }

        @Test
        void 특정_회원이_작성한_게시글을_검색할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            Long postId = createTestPublishedPost();

            Long memberId = getMemberIdFromPost(postId);
            MvcTestResult result = performPostSearch("memberId=" + memberId);

            assertThatMemberSearchResults(result, memberId, 1);
        }

        @Test
        void 게시글_상태로_검색할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();
            createTestDraftPost();

            MvcTestResult result = performPostSearch("status=PUBLISHED");

            assertThatStatusSearchResults(result, PostStatus.PUBLISHED, 1);
        }

        @Test
        void 여러_조건을_조합하여_검색할_수_있다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();
            createTestDraftPost();

            MvcTestResult result = performPostSearch("category=TECH&status=PUBLISHED&keyword=테스트");

            assertThatCombinedSearchResults(result, 1);
        }

        @Test
        void 검색_조건에_맞는_게시글이_없으면_빈_배열을_반환한다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();

            MvcTestResult result = performPostSearch("keyword=존재하지않는키워드");

            assertThatEmptySearchResults(result);
        }

        @Test
        void 검색_조건_없이_요청_시_공개된_게시글만_반환된다()
                throws UnsupportedEncodingException, JsonProcessingException {
            createTestPublishedPost();
            createTestDraftPost();

            MvcTestResult result = performPostSearch("");

            assertThatPublicSearchResults(result, 1);
        }

        private void assertThatPublicSearchResults(MvcTestResult result, PostCategory expectedCategory, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.category() == expectedCategory);
            assertThat(posts).allMatch(post -> post.status() == PostStatus.PUBLISHED);
        }

        private void assertThatPublicSearchResults(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.status() == PostStatus.PUBLISHED);
        }

        private void assertThatOwnerSearchResults(MvcTestResult result, PostCategory expectedCategory, Long expectedMemberId, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.category() == expectedCategory);
            assertThat(posts).allMatch(post -> post.authorId().equals(expectedMemberId));
        }

        private void assertThatKeywordSearchResults(MvcTestResult result, String expectedKeyword, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.title().contains(expectedKeyword));
        }

        private void assertThatTitleKeywordSearchResults(MvcTestResult result, String expectedKeyword, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.title().contains(expectedKeyword));
        }

        private void assertThatContentKeywordSearchResults(MvcTestResult result, String expectedKeyword, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.body().contains(expectedKeyword));
        }

        private void assertThatMemberSearchResults(MvcTestResult result, Long expectedMemberId, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.authorId().equals(expectedMemberId));
        }

        private void assertThatStatusSearchResults(MvcTestResult result, PostStatus expectedStatus, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.status() == expectedStatus);
        }

        private void assertThatCombinedSearchResults(MvcTestResult result, int expectedSize)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).hasSize(expectedSize);
            assertThat(posts).allMatch(post -> post.category() == PostCategory.TECH);
            assertThat(posts).allMatch(post -> post.status() == PostStatus.PUBLISHED);
            assertThat(posts).allMatch(post -> post.title().contains("테스트"));
        }

        private void assertThatEmptySearchResults(MvcTestResult result)
                throws UnsupportedEncodingException, JsonProcessingException {
            assertThat(result).hasStatusOk();

            PostDetailResponse[] posts = parseResponseArray(result, PostDetailResponse[].class);
            assertThat(posts).isEmpty();
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
        MemberRegisterRequest secondMemberRequest = createSecondMemberRegisterRequest();
        memberRegister.register(secondMemberRequest);

        MemberAuthRequest authRequest = createSecondMemberAuthRequest();
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
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = parseResponse(createResult, PostCreateResponse.class);
        return createResponse.postId();
    }

    private Long createTestDraftPost() throws JsonProcessingException, UnsupportedEncodingException {
        PostCreateRequest createRequest = createPostRequest(false);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcTestResult createResult = mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = parseResponse(createResult, PostCreateResponse.class);
        return createResponse.postId();
    }

    private Long getMemberIdFromPost(Long postId) throws UnsupportedEncodingException, JsonProcessingException {
        MvcTestResult postResult = performPostGet(postId);
        PostDetailResponse postDetail = parseResponse(postResult, PostDetailResponse.class);
        return postDetail.authorId();
    }

    // API 호출 헬퍼 메서드들
    private MvcTestResult performPostCreate(PostCreateRequest request, String token) throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();
    }

    private MvcTestResult performPostCreateWithoutToken(PostCreateRequest request) throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.post().uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();
    }

    private MvcTestResult performPostGet(Long postId) {
        return mvcTester.get().uri("/api/posts/{id}", postId).exchange();
    }

    private MvcTestResult performPostGet(Long postId, String token) {
        return mvcTester.get().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

    private MvcTestResult performPostUpdate(Long postId, PostUpdateRequest request, String token)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/posts/{id}", postId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();
    }

    private MvcTestResult performPostUpdateWithoutToken(Long postId, PostUpdateRequest request)
            throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return mvcTester.put().uri("/api/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();
    }

    private MvcTestResult performPostPublish(Long postId, String token) {
        return mvcTester.post().uri("/api/posts/{id}/publish", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

    private MvcTestResult performPostPublishWithoutToken(Long postId) {
        return mvcTester.post().uri("/api/posts/{id}/publish", postId).exchange();
    }

    private MvcTestResult performPostHide(Long postId, String token) {
        return mvcTester.post().uri("/api/posts/{id}/hide", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

    private MvcTestResult performPostHideWithoutToken(Long postId) {
        return mvcTester.post().uri("/api/posts/{id}/hide", postId).exchange();
    }

    private MvcTestResult performPostDelete(Long postId, String token) {
        return mvcTester.post().uri("/api/posts/{id}/delete", postId)
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

    private MvcTestResult performPostDeleteWithoutToken(Long postId) {
        return mvcTester.post().uri("/api/posts/{id}/delete", postId).exchange();
    }

    private MvcTestResult performPostList() {
        return mvcTester.get().uri("/api/posts").exchange();
    }

    private MvcTestResult performPostListByCategory(PostCategory category) {
        return mvcTester.get().uri("/api/posts/category/{category}", category).exchange();
    }

    private MvcTestResult performMemberPostList(Long memberId, String token) {
        return mvcTester.get().uri("/api/members/{memberId}/posts", memberId)
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

    private MvcTestResult performMemberPostListWithoutAuth(Long memberId) {
        return mvcTester.get().uri("/api/members/{memberId}/posts", memberId).exchange();
    }

    private MvcTestResult performPostSearch(String queryParams) {
        String uri = queryParams.isEmpty() ? "/api/posts/search" : "/api/posts/search?" + queryParams;
        return mvcTester.get().uri(uri).exchange();
    }

    private MvcTestResult performPostSearchWithAuth(String queryParams, String token) {
        return mvcTester.get().uri("/api/posts/search?" + queryParams)
                .header("Authorization", "Bearer " + token)
                .exchange();
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