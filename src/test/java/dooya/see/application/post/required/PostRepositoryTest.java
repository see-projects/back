package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostSearchRequest;
import dooya.see.domain.post.PostStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
record PostRepositoryTest(PostRepository postRepository, EntityManager entityManager) {
    @Test
    void 게시물_생성_시_ID가_자동_생성되고_영속화_후_조회가_가능하다() {
        Post post = Post.create(createPostRequest(), 1L);

        assertThat(post.getId()).isNull();

        postRepository.save(post);

        assertThat(post.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        var found = postRepository.findById(post.getId()).orElseThrow();
        assertThat(found.getMemberId()).isEqualTo(1L);
        assertThat(found.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(found.getMetaData().createdAt()).isNotNull();
    }

    @Test
    void 특정_회원이_작성한_게시물들을_모두_조회할_수_있다() {
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(1L);
        Post post3 = createAndSavePost(2L);

        entityManager.flush();
        entityManager.clear();

        List<Post> found = postRepository.findByMemberId(1L);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getId)
            .containsExactlyInAnyOrder(post1.getId(), post2.getId());
        assertThat(found).allMatch(post -> post.getMemberId().equals(1L));
    }

    @Test
    void 존재하지_않는_회원_ID로_조회_시_빈_리스트를_반환한다() {
        List<Post> found = postRepository.findByMemberId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 특정_카테고리의_게시물들을_모두_조회할_수_있다() {
        Post post1 = createAndSavePostWithCategory(1L, PostCategory.TECH);
        Post post2 = createAndSavePostWithCategory(2L, PostCategory.TECH);
        Post post3 = createAndSavePostWithCategory(3L, PostCategory.QNA);

        entityManager.flush();
        entityManager.clear();

        List<Post> found = postRepository.findByCategory(PostCategory.TECH);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getId)
            .containsExactlyInAnyOrder(post1.getId(), post2.getId());
        assertThat(found).allMatch(post -> post.getCategory() == PostCategory.TECH);
    }

    @Test
    void 특정_상태의_게시물들을_모두_조회할_수_있다() {
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(2L);
        post2.publish();
        postRepository.save(post2);

        entityManager.flush();
        entityManager.clear();

        List<Post> draftPosts = postRepository.findByStatus(PostStatus.DRAFT);
        List<Post> publishedPosts = postRepository.findByStatus(PostStatus.PUBLISHED);

        assertThat(draftPosts).hasSize(1);
        assertThat(draftPosts.get(0).getId()).isEqualTo(post1.getId());
        
        assertThat(publishedPosts).hasSize(1);
        assertThat(publishedPosts.get(0).getId()).isEqualTo(post2.getId());
    }

    @Test
    void 카테고리와_상태_조건을_모두_만족하는_게시물들을_조회할_수_있다() {
        // DRAFT 상태로 생성
        Post post1 = createAndSavePostWithCategoryDraft(1L, PostCategory.TECH);
        Post post2 = createAndSavePostWithCategoryDraft(2L, PostCategory.TECH);
        Post post3 = createAndSavePostWithCategoryDraft(3L, PostCategory.QNA);
        
        // DRAFT 상태의 게시물을 PUBLISHED 상태로 변경
        post2.publish();
        postRepository.save(post2);
        post3.publish();
        postRepository.save(post3);

        entityManager.flush();
        entityManager.clear();

        List<Post> found = postRepository.findByCategoryAndStatus(PostCategory.TECH, PostStatus.PUBLISHED);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post2.getId());
        assertThat(found.get(0).getCategory()).isEqualTo(PostCategory.TECH);
        assertThat(found.get(0).getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void 키워드로_제목과_내용을_통합_검색할_수_있다() {
        Post post1 = createAndSavePostWithTitleAndBody(1L, "Spring Boot 튜토리얼", "Spring 내용");
        Post post2 = createAndSavePostWithTitleAndBody(2L, "Java 기초", "Spring Security 가이드");
        Post post3 = createAndSavePostWithTitleAndBody(3L, "Python 입문", "Django 프레임워크");

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("Spring")
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getId)
            .containsExactlyInAnyOrder(post1.getId(), post2.getId());
    }

    @Test
    void 제목_키워드로만_검색할_수_있다() {
        Post post1 = createAndSavePostWithTitleAndBody(1L, "Spring Boot 튜토리얼", "내용1");
        Post post2 = createAndSavePostWithTitleAndBody(2L, "Java 기초", "Spring 내용");

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .titleKeyword("Spring")
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post1.getId());
    }

    @Test
    void 내용_키워드로만_검색할_수_있다() {
        Post post1 = createAndSavePostWithTitleAndBody(1L, "제목1", "Spring Boot 내용");
        Post post2 = createAndSavePostWithTitleAndBody(2L, "Spring 제목", "Java 내용");

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .contentKeyword("Spring")
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post1.getId());
    }

    @Test
    void 카테고리_조건으로_검색할_수_있다() {
        Post post1 = createAndSavePostWithCategory(1L, PostCategory.TECH);
        Post post2 = createAndSavePostWithCategory(2L, PostCategory.QNA);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .category(PostCategory.TECH)
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post1.getId());
    }

    @Test
    void 작성자_ID로_검색할_수_있다() {
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(2L);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .memberId(1L)
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post1.getId());
    }

    @Test
    void 상태_조건으로_검색할_수_있다() {
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(2L);
        post2.publish();
        postRepository.save(post2);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .status(PostStatus.PUBLISHED)
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post2.getId());
    }

    @Test
    void 날짜_범위로_검색할_수_있다() {
        LocalDateTime baseTime = LocalDateTime.now().minusDays(10);
        
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(2L);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .fromDate(baseTime)
                .toDate(LocalDateTime.now())
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(2);
    }

    @Test
    void 복합_조건으로_검색할_수_있다() {
        Post post1 = createAndSavePostWithTitleAndCategory(1L, "Spring 튜토리얼", PostCategory.TECH);
        Post post2 = createAndSavePostWithTitleAndCategory(1L, "Spring 가이드", PostCategory.QNA);
        Post post3 = createAndSavePostWithTitleAndCategory(2L, "Spring 기초", PostCategory.TECH);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("Spring")
                .memberId(1L)
                .category(PostCategory.TECH)
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(post1.getId());
    }

    @Test
    void 검색_조건이_모두_비어있으면_전체_게시물을_반환한다() {
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(2L);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder().build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(2);
    }

    @Test
    void 검색_조건에_맞는_게시물이_없으면_빈_리스트를_반환한다() {
        Post post1 = createAndSavePost(1L);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("존재하지않는키워드")
                .build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).isEmpty();
    }

    // Helper methods
    private Post createAndSavePost(Long memberId) {
        Post post = Post.create(createPostRequest(), memberId);
        return postRepository.save(post);
    }

    private Post createAndSavePostWithCategory(Long memberId, PostCategory category) {
        Post post = Post.create(createPostRequestWithCategory("제목", "내용", category), memberId);
        return postRepository.save(post);
    }

    private Post createAndSavePostWithCategoryDraft(Long memberId, PostCategory category) {
        Post post = Post.create(createPostRequestWithCategory("제목", "내용", category, false), memberId);
        return postRepository.save(post);
    }

    private Post createAndSavePostWithTitleAndBody(Long memberId, String title, String body) {
        Post post = Post.create(createDraftPostRequest(title, body), memberId);
        return postRepository.save(post);
    }

    private Post createAndSavePostWithTitleAndCategory(Long memberId, String title, PostCategory category) {
        Post post = Post.create(createPostRequestWithCategory(title, "내용", category), memberId);
        return postRepository.save(post);
    }
}
