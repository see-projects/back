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

    private Post createPost() {
        return postManager.create(createPostRequest(), 1L);
    }
}
