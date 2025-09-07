package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentFixture;
import dooya.see.domain.post.CommentStatus;
import dooya.see.domain.post.Post;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
public record CommentFinderTest(
        CommentFinder commentFinder,
        CommentManager commentManager,
        CommentRepository commentRepository,
        PostManager postManager,
        PostFinder postFinder,
        EntityManager entityManager) {
    @DisplayName("")
    @Test
    void a() {
        Post post = createPost();
        createComment(post.getId());

        Comment comment = commentFinder.find(post.getId());

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getContent().text()).isEqualTo("테스트 게시물");
        assertThat(comment.getPostId()).isNotNull();
        assertThat(comment.getMemberId()).isNotNull();
        assertThat(comment.getParentCommentId()).isNull();
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(comment.getMetaData().createdAt()).isNotNull();
        assertThat(comment.getMetaData().modifiedAt()).isNull();
    }

    private Post createPost() {
        Post post = postManager.create(createPostRequest(), 1L);
        entityManager.flush();
        entityManager.clear();

        return postFinder.find(post.getId());
    }

    private Comment createComment(Long postId) {
        Comment comment = commentManager.create(CommentFixture.createCommentRequest(), postId, 1L);
        entityManager.flush();
        entityManager.clear();

        return commentFinder.find(comment.getId());
    }
}
