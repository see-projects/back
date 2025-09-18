package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostSearchRequest;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.exception.PostNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostFinderTest(PostFinder postFinder, PostManager postManager, EntityManager entityManager) {
    private static final Long AUTHOR_ID = 1L;
    private static final Long ANOTHER_AUTHOR_ID = 2L;
    private static final Long VIEWER_ID = 2L;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 게시글_단건_조회 {
        @Test
        void ID로_게시글을_조회한다() {
            Post post = createTestPost();

            Post found = postFinder.find(post.getId());

            assertThatPostFound(found, post);
        }

        @Test
        void 존재하지_않는_ID로_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> postFinder.find(999L))
                    .isInstanceOf(PostNotFoundException.class);
        }

        private void assertThatPostFound(Post found, Post expected) {
            assertThat(found.getId()).isEqualTo(expected.getId());
            assertThat(found.getMemberId()).isEqualTo(expected.getMemberId());
            assertThat(found.getContent().title()).isEqualTo(expected.getContent().title());
        }
    }

    @Nested
    class 기본_조회_기능 {
        @Test
        void 회원_ID로_작성한_게시글을_조회한다() {
            Post post1 = createTestPost();
            Post post2 = createTestPost();
            flushAndClearContext();

            List<Post> found = postFinder.findByMemberId(AUTHOR_ID);

            assertThatPostsFoundByMember(found, AUTHOR_ID, post1, post2);
        }

        @Test
        void 게시글이_없는_회원_ID로_조회_시_빈_목록을_반환한다() {
            List<Post> found = postFinder.findByMemberId(999L);

            assertThat(found).isEmpty();
        }

        @Test
        void 카테고리로_게시글을_조회한다() {
            Post post1 = createTestPost();
            Post post2 = createTestPost();
            flushAndClearContext();

            List<Post> found = postFinder.findByCategory(PostCategory.TECH);

            assertThatPostsFoundByCategory(found, PostCategory.TECH, post1, post2);
        }

        @Test
        void 게시글이_없는_카테고리로_조회_시_빈_목록을_반환한다() {
            List<Post> found = postFinder.findByCategory(PostCategory.QNA);

            assertThat(found).isEmpty();
        }

        @Test
        void 상태로_게시글을_조회한다() {
            Post post1 = createTestPost();
            Post post2 = createTestPost();
            flushAndClearContext();

            List<Post> found = postFinder.findByStatus(PostStatus.DRAFT);

            assertThatPostsFoundByStatus(found, PostStatus.DRAFT, post1, post2);
        }

        @Test
        void 해당_상태의_게시글이_없으면_빈_목록을_반환한다() {
            List<Post> found = postFinder.findByStatus(PostStatus.PUBLISHED);

            assertThat(found).isEmpty();
        }

        private void assertThatPostsFoundByMember(List<Post> posts, Long expectedMemberId, Post... expectedPosts) {
            assertThat(posts).hasSize(expectedPosts.length);
            assertThat(posts).extracting(Post::getId)
                    .containsExactlyInAnyOrder(extractIds(expectedPosts));
            assertThat(posts).allMatch(post -> post.getMemberId().equals(expectedMemberId));
        }

        private void assertThatPostsFoundByCategory(List<Post> posts, PostCategory expectedCategory, Post... expectedPosts) {
            assertThat(posts).hasSize(expectedPosts.length);
            assertThat(posts).extracting(Post::getCategory)
                    .containsOnly(expectedCategory);
            assertThat(posts).extracting(Post::getId)
                    .containsExactlyInAnyOrder(extractIds(expectedPosts));
        }

        private void assertThatPostsFoundByStatus(List<Post> posts, PostStatus expectedStatus, Post... expectedPosts) {
            assertThat(posts).hasSize(expectedPosts.length);
            assertThat(posts).extracting(Post::getStatus)
                    .containsOnly(expectedStatus);
            assertThat(posts).extracting(Post::getId)
                    .containsExactlyInAnyOrder(extractIds(expectedPosts));
        }
    }

    @Nested
    class 공개_게시글_조회 {
        @Test
        void 발행된_게시글만_공개_조회할_수_있다() {
            Post post1 = createPublishedPost();
            Post post2 = createPublishedPost();
            flushAndClearContext();

            List<Post> found = postFinder.findPublicPosts();

            assertThatPublicPostsFound(found, post1, post2);
        }

        @Test
        void 발행된_게시글이_없으면_빈_목록을_반환한다() {
            List<Post> found = postFinder.findPublicPosts();

            assertThat(found).isEmpty();
        }

        @Test
        void 특정_카테고리의_발행된_게시글만_조회할_수_있다() {
            Post post1 = createPublishedPost();
            Post post2 = createPublishedPost();
            flushAndClearContext();

            List<Post> found = postFinder.findPublicPostsByCategory(PostCategory.TECH);

            assertThatPublicPostsByCategoryFound(found, PostCategory.TECH, post1, post2);
        }

        @Test
        void 해당_카테고리에_발행된_게시글이_없으면_빈_목록을_반환한다() {
            List<Post> found = postFinder.findPublicPostsByCategory(PostCategory.TECH);

            assertThat(found).isEmpty();
        }

        private void assertThatPublicPostsFound(List<Post> posts, Post... expectedPosts) {
            assertThat(posts).hasSize(expectedPosts.length);
            assertThat(posts).extracting(Post::getStatus)
                    .containsOnly(PostStatus.PUBLISHED);
            assertThat(posts).extracting(Post::getId)
                    .containsExactlyInAnyOrder(extractIds(expectedPosts));
        }

        private void assertThatPublicPostsByCategoryFound(List<Post> posts, PostCategory expectedCategory, Post... expectedPosts) {
            assertThat(posts).hasSize(expectedPosts.length);
            assertThat(posts).extracting(Post::getStatus)
                    .containsOnly(PostStatus.PUBLISHED);
            assertThat(posts).extracting(Post::getCategory)
                    .containsOnly(expectedCategory);
            assertThat(posts).extracting(Post::getId)
                    .containsExactlyInAnyOrder(extractIds(expectedPosts));
        }
    }

    @Nested
    class 게시글_검색 {
        @Test
        void 제목_키워드로_검색한다() {
            createTestPostWithTitle("Spring Boot 튜토리얼");
            createTestPostWithTitle("Spring Security 가이드");
            createTestPostWithTitle("Java 기초");
            flushAndClearContext();

            List<Post> found = searchByTitleKeyword("Spring");

            assertThatSearchResultContainsTitles(found, "Spring Boot 튜토리얼", "Spring Security 가이드");
        }

        @Test
        void 전체_키워드로_제목과_내용을_통합_검색한다() {
            createTestPostWithContent("테스트 게시물", "Spring 내용");
            flushAndClearContext();

            List<Post> found = searchByKeyword("Spring");

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getContent().title()).isEqualTo("테스트 게시물");
        }

        @Test
        void 내용_키워드로_게시물_본문을_검색한다() {
            createTestPostWithContent("제목1", "JPA 사용법 설명");
            createTestPostWithContent("제목2", "Hibernate와 JPA 비교");
            createTestPostWithContent("제목3", "Spring 기초");
            flushAndClearContext();

            List<Post> found = searchByContentKeyword("JPA");

            assertThat(found).hasSize(2);
            assertThat(found).extracting(post -> post.getContent().body())
                    .allMatch(content -> content.contains("JPA"));
        }

        @Test
        void 키워드와_작성자와_카테고리로_복합_검색한다() {
            createTestPostWithContent("Spring 튜토리얼", "내용");
            createTestPostByMember("Java 기초", ANOTHER_AUTHOR_ID);
            flushAndClearContext();

            List<Post> found = searchByKeywordAndMemberAndCategory("Spring", AUTHOR_ID, PostCategory.TECH);

            assertThat(found).hasSize(1);
            assertThat(found.getFirst().getContent().title()).isEqualTo("Spring 튜토리얼");
            assertThat(found.getFirst().getMemberId()).isEqualTo(AUTHOR_ID);
        }

        @Test
        void 카테고리별로_게시물을_필터링한다() {
            createTestPostWithCategory("기술글", PostCategory.TECH);
            createTestPostWithCategory("질문글", PostCategory.QNA);
            createTestPostWithCategory("일반글", PostCategory.GENERAL);
            flushAndClearContext();

            List<Post> found = searchByCategory(PostCategory.TECH);

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getCategory()).isEqualTo(PostCategory.TECH);
            assertThat(found.get(0).getContent().title()).isEqualTo("기술글");
        }

        @Test
        void 발행_상태의_게시물만_검색한다() {
            createPublishedPost();
            createTestPost(); // DRAFT 상태
            flushAndClearContext();

            List<Post> found = searchByStatus(PostStatus.PUBLISHED);

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getStatus()).isEqualTo(PostStatus.PUBLISHED);
        }

        @Test
        void 검색_조건에_맞는_게시물이_없으면_빈_결과를_반환한다() {
            createTestPostWithTitle("Java 기초");
            flushAndClearContext();

            List<Post> found = searchByKeyword("Python");

            assertThat(found).isEmpty();
        }

        @Test
        void 모든_검색_조건이_비어있으면_전체_게시물을_반환한다() {
            createTestPostWithTitle("제목1");
            createTestPostWithTitle("제목2");
            flushAndClearContext();

            List<Post> found = searchWithEmptyConditions();

            assertThat(found).hasSize(2);
        }

        private void assertThatSearchResultContainsTitles(List<Post> posts, String... expectedTitles) {
            assertThat(posts).hasSize(expectedTitles.length);
            assertThat(posts).extracting(post -> post.getContent().title())
                    .containsExactlyInAnyOrder(expectedTitles);
        }
    }

    @Nested
    class 게시글_조회_이벤트 {
        @Test
        void 게시글_조회하면_조회_이벤트가_발행된다() {
            Post post = createTestPost();

            Post result = postFinder.viewPost(post.getId(), VIEWER_ID);

            assertThatPostViewed(result, post);
        }

        @Test
        void 익명_사용자가_게시글을_조회할_수_있다() {
            Post post = createTestPost();

            Post result = postFinder.viewPost(post.getId(), null);

            assertThatPostViewed(result, post);
        }

        @Test
        void 존재하지_않는_게시글_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> postFinder.viewPost(999L, VIEWER_ID))
                    .isInstanceOf(PostNotFoundException.class);
        }

        private void assertThatPostViewed(Post result, Post expected) {
            assertThat(result.getId()).isEqualTo(expected.getId());
            assertThat(result.getMemberId()).isEqualTo(expected.getMemberId());
            assertThat(result.getContent().title()).isEqualTo(expected.getContent().title());
        }
    }

    // 헬퍼 메서드들
    private Post createTestPost() {
        return postManager.create(createPostRequest(), AUTHOR_ID);
    }

    private Post createPublishedPost() {
        return postManager.create(createPostRequest(true), AUTHOR_ID);
    }

    private Post createTestPostWithTitle(String title) {
        return postManager.create(createPostRequest(title, "내용"), AUTHOR_ID);
    }

    private Post createTestPostWithContent(String title, String content) {
        return postManager.create(createPostRequest(title, content), AUTHOR_ID);
    }

    private Post createTestPostByMember(String title, Long memberId) {
        return postManager.create(createPostRequest(title, "내용"), memberId);
    }

    private Post createTestPostWithCategory(String title, PostCategory category) {
        return postManager.create(createPostRequestWithCategory(title, "내용", category), AUTHOR_ID);
    }

    private List<Post> searchByTitleKeyword(String keyword) {
        return postFinder.search(PostSearchRequest.builder()
                .titleKeyword(keyword)
                .build());
    }

    private List<Post> searchByKeyword(String keyword) {
        return postFinder.search(PostSearchRequest.builder()
                .keyword(keyword)
                .build());
    }

    private List<Post> searchByContentKeyword(String keyword) {
        return postFinder.search(PostSearchRequest.builder()
                .contentKeyword(keyword)
                .build());
    }

    private List<Post> searchByKeywordAndMemberAndCategory(String keyword, Long memberId, PostCategory category) {
        return postFinder.search(PostSearchRequest.builder()
                .keyword(keyword)
                .memberId(memberId)
                .category(category)
                .build());
    }

    private List<Post> searchByCategory(PostCategory category) {
        return postFinder.search(PostSearchRequest.builder()
                .category(category)
                .build());
    }

    private List<Post> searchByStatus(PostStatus status) {
        return postFinder.search(PostSearchRequest.builder()
                .status(status)
                .build());
    }

    private List<Post> searchWithEmptyConditions() {
        return postFinder.search(PostSearchRequest.builder().build());
    }

    private Long[] extractIds(Post... posts) {
        return java.util.Arrays.stream(posts)
                .map(Post::getId)
                .toArray(Long[]::new);
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}