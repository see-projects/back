package dooya.see.application.post.required;

import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
record PostRepositoryTest(PostRepository postRepository, EntityManager entityManager) {
    
    @DisplayName("게시물 생성 시 ID가 자동 생성되고 영속화 후 조회가 가능하다")
    @Test
    void createPost() {
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

    @DisplayName("특정 회원이 작성한 게시물들을 모두 조회할 수 있다")
    @Test
    void findByMemberId() {
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

    @DisplayName("존재하지 않는 회원 ID로 조회 시 빈 리스트를 반환한다")
    @Test
    void findByNonExistentMemberId() {
        List<Post> found = postRepository.findByMemberId(999L);

        assertThat(found).isEmpty();
    }

    @DisplayName("특정 카테고리의 게시물들을 모두 조회할 수 있다")
    @Test
    void findByCategory() {
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

    @DisplayName("특정 상태의 게시물들을 모두 조회할 수 있다")
    @Test
    void findByStatus() {
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

    @DisplayName("카테고리와 상태 조건을 모두 만족하는 게시물들을 조회할 수 있다")
    @Test
    void findByCategoryAndStatus() {
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

    @DisplayName("키워드로 제목과 내용을 통합 검색할 수 있다")
    @Test
    void searchByKeyword() {
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

    @DisplayName("제목 키워드로만 검색할 수 있다")
    @Test
    void searchByTitleKeyword() {
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

    @DisplayName("내용 키워드로만 검색할 수 있다")
    @Test
    void searchByContentKeyword() {
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

    @DisplayName("카테고리 조건으로 검색할 수 있다")
    @Test
    void searchByCategory() {
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

    @DisplayName("작성자 ID로 검색할 수 있다")
    @Test
    void searchByMemberId() {
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

    @DisplayName("상태 조건으로 검색할 수 있다")
    @Test
    void searchByStatus() {
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

    @DisplayName("날짜 범위로 검색할 수 있다")
    @Test
    void searchByDateRange() {
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

    @DisplayName("복합 조건으로 검색할 수 있다")
    @Test
    void searchWithMultipleConditions() {
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

    @DisplayName("검색 조건이 모두 비어있으면 전체 게시물을 반환한다")
    @Test
    void searchWithEmptyConditions() {
        Post post1 = createAndSavePost(1L);
        Post post2 = createAndSavePost(2L);

        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder().build();

        List<Post> found = postRepository.search(searchRequest);

        assertThat(found).hasSize(2);
    }

    @DisplayName("검색 조건에 맞는 게시물이 없으면 빈 리스트를 반환한다")
    @Test
    void searchWithNoResults() {
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
