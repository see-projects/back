package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostFinderTest(PostFinder postFinder, PostManager postManager, EntityManager entityManager) {
    
    @Test
    @DisplayName("게시글 ID로 조회하면 해당 게시글이 반환된다")
    void findPostById() {
        Post post = createPost();
        entityManager.flush();
        entityManager.clear();

        Post found = postFinder.find(post.getId());

        assertThat(found.getId()).isEqualTo(post.getId());
        assertThat(found.getMemberId()).isEqualTo(post.getMemberId());
        assertThat(found.getContent().title()).isEqualTo(post.getContent().title());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 조회하면 PostNotFoundException이 발생한다")
    void findPostByNonExistentId() {
        assertThatThrownBy(() -> postFinder.find(999L))
            .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("특정 회원이 작성한 여러 게시글을 모두 조회할 수 있다")
    void findMultiplePostsByMemberId() {
        Post post1 = createPost();
        Post post2 = createPost();
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.findByMemberId(1L);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getId)
            .containsExactlyInAnyOrder(post1.getId(), post2.getId());
        assertThat(found).allMatch(post -> post.getMemberId().equals(1L));
    }

    @Test
    @DisplayName("게시글을 작성하지 않은 회원 ID로 조회하면 빈 리스트가 반환된다")
    void findPostsByNonExistentMemberId() {
        List<Post> found = postFinder.findByMemberId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("특정 카테고리의 게시글들을 모두 조회할 수 있다")
    void findPostsByCategory() {
        Post post1 = createPost();
        Post post2 = createPost();
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.findByCategory(PostCategory.TECH);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getCategory)
            .containsOnly(PostCategory.TECH);
        assertThat(found).extracting(Post::getId)
            .containsExactlyInAnyOrder(post1.getId(), post2.getId());
    }

    @Test
    @DisplayName("게시글이 없는 카테고리로 조회하면 빈 리스트가 반환된다")
    void findPostsByNonExistentCategory() {
        List<Post> found = postFinder.findByCategory(PostCategory.QNA);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("특정 상태의 게시글들을 모두 조회할 수 있다")
    void findPostsByStatus() {
        Post post1 = createPost();
        Post post2 = createPost();
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.findByStatus(PostStatus.DRAFT);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getStatus)
            .containsOnly(PostStatus.DRAFT);
        assertThat(found).extracting(Post::getId)
            .containsExactlyInAnyOrder(post1.getId(), post2.getId());
    }

    @Test
    @DisplayName("해당 상태의 게시글이 없으면 빈 리스트가 반환된다")
    void findPostsByNonExistentStatus() {
        List<Post> found = postFinder.findByStatus(PostStatus.PUBLISHED);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("발행된 게시글들만 공개 조회할 수 있다")
    void findPublicPosts() {
        Post post1 = createPublicPost();
        Post post2 = createPublicPost();
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.findPublicPosts();

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getStatus)
                .containsOnly(PostStatus.PUBLISHED);
        assertThat(found).extracting(Post::getId)
                .containsExactlyInAnyOrder(post1.getId(), post2.getId());
    }

    @Test
    @DisplayName("발행된 게시글이 없으면 공개 조회 시 빈 리스트가 반환된다")
    void findPublicPostsWhenNonePublished() {
        List<Post> found = postFinder.findPublicPosts();

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("특정 카테고리의 발행된 게시글들만 공개 조회할 수 있다")
    void findPublicPostsByCategory() {
        Post post1 = createPublicPost();
        Post post2 = createPublicPost();
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.findPublicPostsByCategory(PostCategory.TECH);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Post::getStatus)
                .containsOnly(PostStatus.PUBLISHED);
        assertThat(found).extracting(Post::getCategory)
                .containsOnly(PostCategory.TECH);
        assertThat(found).extracting(Post::getId)
                .containsExactlyInAnyOrder(post1.getId(), post2.getId());
    }

    @Test
    @DisplayName("해당 카테고리에 발행된 게시글이 없으면 빈 리스트가 반환된다")
    void findPublicPostsByNonExistentCategory() {
        List<Post> found = postFinder.findPublicPostsByCategory(PostCategory.TECH);
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("키워드로 제목 검색하면 해당 키워드가 포함된 게시물들이 반환된다")
    void searchPostsByTitleKeyword() {
        Post post1 = postManager.create(createPostRequest("Spring Boot 튜토리얼", "내용1"), 1L);
        Post post2 = postManager.create(createPostRequest("Spring Security 가이드", "내용2"), 1L);
        Post post3 = postManager.create(createPostRequest("Java 기초", "내용3"), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .titleKeyword("Spring")
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(post -> post.getContent().title())
            .containsExactlyInAnyOrder("Spring Boot 튜토리얼", "Spring Security 가이드");
    }

    @Test
    @DisplayName("Builder 패턴으로 간단한 키워드 검색이 가능하다")
    void searchWithKeywordBuilder() {
        Post post = postManager.create(createPostRequest("테스트 게시물", "Spring 내용"), 1L);
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.search(PostSearchRequest.builder()
                .keyword("Spring")
                .build());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getContent().title()).isEqualTo("테스트 게시물");
    }

    @Test
    @DisplayName("복합 조건 검색이 Builder로 깔끔하게 표현된다")
    void searchWithComplexConditions() {
        Post post1 = postManager.create(createPostRequest("Spring 튜토리얼", "내용"), 1L);
        Post post2 = postManager.create(createPostRequest("Java 기초", "내용"), 2L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("Spring")
                .memberId(1L)
                .category(PostCategory.TECH)
                .build();
        
        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getContent().title()).isEqualTo("Spring 튜토리얼");
        assertThat(found.get(0).getMemberId()).isEqualTo(1L);
    }

    private Post createPost() {
        return postManager.create(createPostRequest(), 1L);
    }

    private Post createPublicPost() {
        return postManager.create(createPostRequest(true), 1L);
    }
}
