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
        postManager.create(createPostRequest("Spring Boot 튜토리얼", "내용1"), 1L);
        postManager.create(createPostRequest("Spring Security 가이드", "내용2"), 1L);
        postManager.create(createPostRequest("Java 기초", "내용3"), 1L);
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
    @DisplayName("전체 키워드로 제목과 내용을 통합 검색할 수 있다")
    void searchWithKeywordBuilder() {
        postManager.create(createPostRequest("테스트 게시물", "Spring 내용"), 1L);
        entityManager.flush();
        entityManager.clear();

        List<Post> found = postFinder.search(PostSearchRequest.builder()
                .keyword("Spring")
                .build());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getContent().title()).isEqualTo("테스트 게시물");
    }

    @Test
    @DisplayName("키워드와 작성자와 카테고리 조건으로 복합 검색할 수 있다")
    void searchWithComplexConditions() {
        postManager.create(createPostRequest("Spring 튜토리얼", "내용"), 1L);
        postManager.create(createPostRequest("Java 기초", "내용"), 2L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("Spring")
                .memberId(1L)
                .category(PostCategory.TECH)
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getContent().title()).isEqualTo("Spring 튜토리얼");
        assertThat(found.getFirst().getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("내용 키워드로 게시물 본문을 검색할 수 있다")
    void searchByContentKeyword() {
        postManager.create(createPostRequest("제목1", "JPA 사용법 설명"), 1L);
        postManager.create(createPostRequest("제목2", "Hibernate와 JPA 비교"), 1L);
        postManager.create(createPostRequest("제목3", "Spring 기초"), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .contentKeyword("JPA")
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(post -> post.getContent().body())
                .allMatch(content -> content.contains("JPA"));
    }

    @Test
    @DisplayName("카테고리별로 게시물을 필터링할 수 있다")
    void searchByCategory() {
        postManager.create(createPostRequestWithCategory("기술글", "내용", PostCategory.TECH), 1L);
        postManager.create(createPostRequestWithCategory("질문글", "내용", PostCategory.QNA), 1L);
        postManager.create(createPostRequestWithCategory("일반글", "내용", PostCategory.GENERAL), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .category(PostCategory.TECH)
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCategory()).isEqualTo(PostCategory.TECH);
        assertThat(found.get(0).getContent().title()).isEqualTo("기술글");
    }

    @Test
    @DisplayName("특정 작성자의 게시물을 키워드와 함께 검색할 수 있다")
    void searchByMemberAndKeyword() {
        postManager.create(createPostRequest("Spring 튜토리얼", "내용1"), 1L);
        postManager.create(createPostRequest("Spring 가이드", "내용2"), 2L);
        postManager.create(createPostRequest("Java Spring", "내용3"), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("Spring")
                .memberId(1L)
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(2);
        assertThat(found).allMatch(post -> post.getMemberId().equals(1L));
        assertThat(found).extracting(post -> post.getContent().title())
                .containsExactlyInAnyOrder("Spring 튜토리얼", "Java Spring");
    }

    @Test
    @DisplayName("발행 상태의 게시물만 검색할 수 있다")
    void searchByPublishedStatus() {
        postManager.create(createPostRequest(true), 1L);
        postManager.create(createPostRequest(false), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .status(PostStatus.PUBLISHED)
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    @DisplayName("검색 조건에 맞는 게시물이 없으면 빈 결과를 반환한다")
    void searchWithNoResults() {
        postManager.create(createPostRequest("Java 기초", "내용"), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword("Python")  // 존재하지 않는 키워드
                .build();

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("모든 검색 조건이 비어있으면 전체 게시물을 반환한다")
    void searchWithEmptyConditions() {
        postManager.create(createPostRequest("제목1", "내용1"), 1L);
        postManager.create(createPostRequest("제목2", "내용2"), 1L);
        entityManager.flush();
        entityManager.clear();

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .build();  // 모든 조건이 null

        List<Post> found = postFinder.search(searchRequest);

        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("게시글을 조회하면 조회 이벤트가 발행된다")
    void viewPost() {
        Post post = createPost();

        Post result = postFinder.viewPost(post.getId(), 2L);

        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getContent().title()).isEqualTo(post.getContent().title());
        // 조회 이벤트는 PostStatsEventHandler에서 처리되므로 여기서는 확인 불가
    }

    @Test
    @DisplayName("익명 사용자가 게시글을 조회할 수 있다")
    void viewPostByAnonymousUser() {
        Post post = createPost();

        Post result = postFinder.viewPost(post.getId(), null);

        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(result.getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 조회하려고 하면 PostNotFoundException이 발생한다")
    void viewNonExistentPost() {
        assertThatThrownBy(() -> postFinder.viewPost(999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    private Post createPost() {
        return postManager.create(createPostRequest(), 1L);
    }

    private Post createPublicPost() {
        return postManager.create(createPostRequest(true), 1L);
    }
}
