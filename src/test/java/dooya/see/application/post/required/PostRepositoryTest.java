package dooya.see.application.post.required;

import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
record PostRepositoryTest(PostRepository postRepository, EntityManager entityManager) {
    private static final Long MEMBER_ID = 1L;
    private static final Long ANOTHER_MEMBER_ID = 2L;
    private static final Long THIRD_MEMBER_ID = 3L;
    private static final String SPRING_KEYWORD = "Spring";

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 게시물_기본_CRUD {
        @Test
        void 게시물_생성_및_조회가_성공한다() {
            Post post = createTestPost(MEMBER_ID);

            assertThatPostCreated(post, MEMBER_ID);
        }

        @Test
        void 존재하지_않는_회원_ID로_조회_시_빈_리스트를_반환한다() {
            List<Post> found = postRepository.findByMemberId(999L);

            assertThat(found).isEmpty();
        }

        private void assertThatPostCreated(Post post, Long expectedMemberId) {
            assertThat(post.getId()).isNotNull();

            flushAndClearContext();
            var found = postRepository.findById(post.getId()).orElseThrow();

            assertThat(found.getMemberId()).isEqualTo(expectedMemberId);
            assertThat(found.getStatus()).isEqualTo(PostStatus.DRAFT);
            assertThat(found.getMetaData().createdAt()).isNotNull();
        }
    }

    @Nested
    class 회원별_게시물_조회 {
        @Test
        void 특정_회원의_게시물들을_조회할_수_있다() {
            Post post1 = createAndSavePost(MEMBER_ID);
            Post post2 = createAndSavePost(MEMBER_ID);
            Post post3 = createAndSavePost(ANOTHER_MEMBER_ID);

            List<Post> found = postRepository.findByMemberId(MEMBER_ID);

            assertThatMemberPostsFound(found, post1, post2);
        }

        private void assertThatMemberPostsFound(List<Post> found, Post post1, Post post2) {
            assertThat(found).hasSize(2);
            assertThat(found).extracting(Post::getId)
                    .containsExactlyInAnyOrder(post1.getId(), post2.getId());
            assertThat(found).allMatch(post -> post.getMemberId().equals(MEMBER_ID));
        }
    }

    @Nested
    class 카테고리별_게시물_조회 {
        @Test
        void 특정_카테고리의_게시물들을_조회할_수_있다() {
            Post post1 = createAndSavePostWithCategory(MEMBER_ID, PostCategory.TECH);
            Post post2 = createAndSavePostWithCategory(ANOTHER_MEMBER_ID, PostCategory.TECH);
            Post post3 = createAndSavePostWithCategory(THIRD_MEMBER_ID, PostCategory.QNA);

            List<Post> found = postRepository.findByCategory(PostCategory.TECH);

            assertThatCategoryPostsFound(found, post1, post2, PostCategory.TECH);
        }

        private void assertThatCategoryPostsFound(List<Post> found, Post post1, Post post2, PostCategory expectedCategory) {
            assertThat(found).hasSize(2);
            assertThat(found).extracting(Post::getId)
                    .containsExactlyInAnyOrder(post1.getId(), post2.getId());
            assertThat(found).allMatch(post -> post.getCategory() == expectedCategory);
        }
    }

    @Nested
    class 상태별_게시물_조회 {
        @Test
        void 특정_상태의_게시물들을_조회할_수_있다() {
            Post draftPost = createAndSavePost(MEMBER_ID);
            Post publishedPost = createAndSavePost(ANOTHER_MEMBER_ID);
            publishPost(publishedPost);

            List<Post> draftPosts = postRepository.findByStatus(PostStatus.DRAFT);
            List<Post> publishedPosts = postRepository.findByStatus(PostStatus.PUBLISHED);

            assertThatStatusPostsFound(draftPosts, draftPost, publishedPosts, publishedPost);
        }

        @Test
        void 카테고리와_상태_조건을_모두_만족하는_게시물들을_조회할_수_있다() {
            Post techDraftPost = createAndSavePostWithCategoryDraft(MEMBER_ID, PostCategory.TECH);
            Post techPublishedPost = createAndSavePostWithCategoryDraft(ANOTHER_MEMBER_ID, PostCategory.TECH);
            Post qnaPublishedPost = createAndSavePostWithCategoryDraft(THIRD_MEMBER_ID, PostCategory.QNA);

            publishPost(techPublishedPost);
            publishPost(qnaPublishedPost);

            List<Post> found = postRepository.findByCategoryAndStatus(PostCategory.TECH, PostStatus.PUBLISHED);

            assertThatCategoryAndStatusPostsFound(found, techPublishedPost);
        }

        private void assertThatStatusPostsFound(List<Post> draftPosts, Post expectedDraftPost,
                                                List<Post> publishedPosts, Post expectedPublishedPost) {
            assertThat(draftPosts).hasSize(1);
            assertThat(draftPosts.get(0).getId()).isEqualTo(expectedDraftPost.getId());

            assertThat(publishedPosts).hasSize(1);
            assertThat(publishedPosts.get(0).getId()).isEqualTo(expectedPublishedPost.getId());
        }

        private void assertThatCategoryAndStatusPostsFound(List<Post> found, Post expectedPost) {
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getId()).isEqualTo(expectedPost.getId());
            assertThat(found.get(0).getCategory()).isEqualTo(PostCategory.TECH);
            assertThat(found.get(0).getStatus()).isEqualTo(PostStatus.PUBLISHED);
        }
    }

    @Nested
    class 키워드_검색 {
        @Test
        void 키워드로_제목과_내용을_통합_검색할_수_있다() {
            Post springTitlePost = createAndSavePostWithTitleAndBody(MEMBER_ID, "Spring Boot 튜토리얼", "Spring 내용");
            Post springContentPost = createAndSavePostWithTitleAndBody(ANOTHER_MEMBER_ID, "Java 기초", "Spring Security 가이드");
            Post pythonPost = createAndSavePostWithTitleAndBody(THIRD_MEMBER_ID, "Python 입문", "Django 프레임워크");

            PostSearchRequest searchRequest = createSearchRequest().keyword(SPRING_KEYWORD).build();
            List<Post> found = postRepository.search(searchRequest);

            assertThatKeywordSearchFound(found, springTitlePost, springContentPost);
        }

        @Test
        void 제목_키워드로만_검색할_수_있다() {
            Post springTitlePost = createAndSavePostWithTitleAndBody(MEMBER_ID, "Spring Boot 튜토리얼", "내용1");
            Post springContentPost = createAndSavePostWithTitleAndBody(ANOTHER_MEMBER_ID, "Java 기초", "Spring 내용");

            PostSearchRequest searchRequest = createSearchRequest().titleKeyword(SPRING_KEYWORD).build();
            List<Post> found = postRepository.search(searchRequest);

            assertThatSinglePostFound(found, springTitlePost);
        }

        @Test
        void 내용_키워드로만_검색할_수_있다() {
            Post springContentPost = createAndSavePostWithTitleAndBody(MEMBER_ID, "제목1", "Spring Boot 내용");
            Post springTitlePost = createAndSavePostWithTitleAndBody(ANOTHER_MEMBER_ID, "Spring 제목", "Java 내용");

            PostSearchRequest searchRequest = createSearchRequest().contentKeyword(SPRING_KEYWORD).build();
            List<Post> found = postRepository.search(searchRequest);

            assertThatSinglePostFound(found, springContentPost);
        }

        private void assertThatKeywordSearchFound(List<Post> found, Post post1, Post post2) {
            assertThat(found).hasSize(2);
            assertThat(found).extracting(Post::getId)
                    .containsExactlyInAnyOrder(post1.getId(), post2.getId());
        }

        private void assertThatSinglePostFound(List<Post> found, Post expectedPost) {
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getId()).isEqualTo(expectedPost.getId());
        }
    }

    @Nested
    class 조건별_검색 {
        @Test
        void 카테고리_조건으로_검색할_수_있다() {
            Post techPost = createAndSavePostWithCategory(MEMBER_ID, PostCategory.TECH);
            Post qnaPost = createAndSavePostWithCategory(ANOTHER_MEMBER_ID, PostCategory.QNA);

            PostSearchRequest searchRequest = createSearchRequest().category(PostCategory.TECH).build();
            List<Post> found = postRepository.search(searchRequest);

            assertThatSinglePostFound(found, techPost);
        }

        @Test
        void 작성자_ID로_검색할_수_있다() {
            Post memberPost = createAndSavePost(MEMBER_ID);
            Post anotherMemberPost = createAndSavePost(ANOTHER_MEMBER_ID);

            PostSearchRequest searchRequest = createSearchRequest().memberId(MEMBER_ID).build();
            List<Post> found = postRepository.search(searchRequest);

            assertThatSinglePostFound(found, memberPost);
        }

        @Test
        void 상태_조건으로_검색할_수_있다() {
            Post draftPost = createAndSavePost(MEMBER_ID);
            Post publishedPost = createAndSavePost(ANOTHER_MEMBER_ID);
            publishPost(publishedPost);

            PostSearchRequest searchRequest = createSearchRequest().status(PostStatus.PUBLISHED).build();
            List<Post> found = postRepository.search(searchRequest);

            assertThatSinglePostFound(found, publishedPost);
        }

        @Test
        void 날짜_범위로_검색할_수_있다() {
            LocalDateTime baseTime = LocalDateTime.now().minusDays(10);

            Post post1 = createAndSavePost(MEMBER_ID);
            Post post2 = createAndSavePost(ANOTHER_MEMBER_ID);

            PostSearchRequest searchRequest = createSearchRequest()
                    .fromDate(baseTime)
                    .toDate(LocalDateTime.now())
                    .build();

            List<Post> found = postRepository.search(searchRequest);

            assertThat(found).hasSize(2);
        }

        private void assertThatSinglePostFound(List<Post> found, Post expectedPost) {
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getId()).isEqualTo(expectedPost.getId());
        }
    }

    @Nested
    class 복합_검색 {
        @Test
        void 복합_조건으로_검색할_수_있다() {
            Post targetPost = createAndSavePostWithTitleAndCategory(MEMBER_ID, "Spring 튜토리얼", PostCategory.TECH);
            Post wrongCategoryPost = createAndSavePostWithTitleAndCategory(MEMBER_ID, "Spring 가이드", PostCategory.QNA);
            Post wrongMemberPost = createAndSavePostWithTitleAndCategory(ANOTHER_MEMBER_ID, "Spring 기초", PostCategory.TECH);

            PostSearchRequest searchRequest = createSearchRequest()
                    .keyword(SPRING_KEYWORD)
                    .memberId(MEMBER_ID)
                    .category(PostCategory.TECH)
                    .build();

            List<Post> found = postRepository.search(searchRequest);

            assertThatSinglePostFound(found, targetPost);
        }

        @Test
        void 검색_조건이_모두_비어있으면_전체_게시물을_반환한다() {
            Post post1 = createAndSavePost(MEMBER_ID);
            Post post2 = createAndSavePost(ANOTHER_MEMBER_ID);

            PostSearchRequest searchRequest = createSearchRequest().build();
            List<Post> found = postRepository.search(searchRequest);

            assertThat(found).hasSize(2);
        }

        @Test
        void 검색_조건에_맞는_게시물이_없으면_빈_리스트를_반환한다() {
            Post post1 = createAndSavePost(MEMBER_ID);

            PostSearchRequest searchRequest = createSearchRequest()
                    .keyword("존재하지않는키워드")
                    .build();

            List<Post> found = postRepository.search(searchRequest);

            assertThat(found).isEmpty();
        }

        private void assertThatSinglePostFound(List<Post> found, Post expectedPost) {
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getId()).isEqualTo(expectedPost.getId());
        }
    }

    // 헬퍼 메서드들
    private Post createTestPost(Long memberId) {
        Post post = Post.create(createPostRequest(), memberId);
        assertThat(post.getId()).isNull();

        postRepository.save(post);
        assertThat(post.getId()).isNotNull();

        return post;
    }

    private Post createAndSavePost(Long memberId) {
        Post post = Post.create(createPostRequest(), memberId);
        Post savedPost = postRepository.save(post);
        flushAndClearContext();
        return savedPost;
    }

    private Post createAndSavePostWithCategory(Long memberId, PostCategory category) {
        Post post = Post.create(createPostRequestWithCategory("제목", "내용", category), memberId);
        Post savedPost = postRepository.save(post);
        flushAndClearContext();
        return savedPost;
    }

    private Post createAndSavePostWithCategoryDraft(Long memberId, PostCategory category) {
        Post post = Post.create(createPostRequestWithCategory("제목", "내용", category, false), memberId);
        Post savedPost = postRepository.save(post);
        flushAndClearContext();
        return savedPost;
    }

    private Post createAndSavePostWithTitleAndBody(Long memberId, String title, String body) {
        Post post = Post.create(createDraftPostRequest(title, body), memberId);
        Post savedPost = postRepository.save(post);
        flushAndClearContext();
        return savedPost;
    }

    private Post createAndSavePostWithTitleAndCategory(Long memberId, String title, PostCategory category) {
        Post post = Post.create(createPostRequestWithCategory(title, "내용", category), memberId);
        Post savedPost = postRepository.save(post);
        flushAndClearContext();
        return savedPost;
    }

    private void publishPost(Post post) {
        post.publish();
        postRepository.save(post);
        flushAndClearContext();
    }

    private PostSearchRequest.PostSearchRequestBuilder createSearchRequest() {
        return PostSearchRequest.builder();
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}