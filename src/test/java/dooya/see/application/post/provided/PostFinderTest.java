package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostSearchRequest;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.exception.PostNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static dooya.see.domain.post.PostFixture.createPostRequestWithCategory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostFinderTest(PostFinder postFinder, PostManager postManager, EntityManager entityManager) {
    @Test
    void 게시글_ID로_조회하면_해당_게시글이_반환된다() {
        Post post = createPost();
        entityManager.flush();
        entityManager.clear();

        Post found = postFinder.find(post.getId());

        assertThat(found.getId()).isEqualTo(post.getId());
        assertThat(found.getMemberId()).isEqualTo(post.getMemberId());
        assertThat(found.getContent().title()).isEqualTo(post.getContent().title());
    }

    @Test
    void 존재하지_않는_게시글_ID로_조회하면_PostNotFoundException이_발생한다() {
        assertThatThrownBy(() -> postFinder.find(999L))
            .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void 특정_회원이_작성한_여러_게시글을_모두_조회할_수_있다() {
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
    void 게시글을_작성하지_않은_회원_ID로_조회하면_빈_리스트가_반환된다() {
        List<Post> found = postFinder.findByMemberId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 특정_카테고리의_게시글들을_모두_조회할_수_있다() {
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
    void 게시글이_없는_카테고리로_조회하면_빈_리스트가_반환된다() {
        List<Post> found = postFinder.findByCategory(PostCategory.QNA);

        assertThat(found).isEmpty();
    }

    @Test
    void 특정_상태의_게시글들을_모두_조회할_수_있다() {
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
    void 해당_상태의_게시글이_없으면_빈_리스트가_반환된다() {
        List<Post> found = postFinder.findByStatus(PostStatus.PUBLISHED);

        assertThat(found).isEmpty();
    }

    @Test
    void 발행된_게시글들만_공개_조회할_수_있다() {
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
    void 발행된_게시글이_없으면_공개_조회_시_빈_리스트가_반환된다() {
        List<Post> found = postFinder.findPublicPosts();

        assertThat(found).isEmpty();
    }

    @Test
    void 특정_카테고리의_발행된_게시글들만_공개_조회할_수_있다() {
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
    void 해당_카테고리에_발행된_게시글이_없으면_빈_리스트가_반환된다() {
        List<Post> found = postFinder.findPublicPostsByCategory(PostCategory.TECH);
        
        assertThat(found).isEmpty();
    }

    @Test
    void 키워드로_제목_검색하면_해당_키워드가_포함된_게시물들이_반환된다() {
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
    void 전체_키워드로_제목과_내용을_통합_검색할_수_있다() {
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
    void 키워드와_작성자와_카테고리_조건으로_복합_검색할_수_있다() {
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
    void 내용_키워드로_게시물_본문을_검색할_수_있다() {
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
    void 카테고리별로_게시물을_필터링할_수_있다() {
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
    void 특정_작성자의_게시물을_키워드와_함께_검색할_수_있다() {
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
    void 발행_상태의_게시물만_검색할_수_있다() {
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
    void 검색_조건에_맞는_게시물이_없으면_빈_결과를_반환한다() {
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
    void 모든_검색_조건이_비어있으면_전체_게시물을_반환한다() {
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
    void 게시글을_조회하면_조회_이벤트가_발행된다() {
        Post post = createPost();

        Post result = postFinder.viewPost(post.getId(), 2L);

        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getContent().title()).isEqualTo(post.getContent().title());
        // 조회 이벤트는 PostStatsEventHandler에서 처리되므로 여기서는 확인 불가
    }

    @Test
    void 익명_사용자가_게시글을_조회할_수_있다() {
        Post post = createPost();

        Post result = postFinder.viewPost(post.getId(), null);

        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(result.getMemberId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_게시글을_조회하려고_하면_PostNotFoundException이_발생한다() {
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
