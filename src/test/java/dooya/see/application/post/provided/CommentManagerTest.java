package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record CommentManagerTest(
        CommentManager commentManager,
        CommentFinder commentFinder,
        CommentRepository commentRepository,
        PostRepository postRepository,
        PostManager postManager,
        PostFinder postFinder,
        EntityManager entityManager) {
    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @DisplayName("")
        @Test
        void a() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getContent().text()).isEqualTo("테스트 게시물");
            assertThat(comment.getPostId()).isNotNull();
            assertThat(comment.getMemberId()).isNotNull();
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNull();
        }

        @DisplayName("")
        @Test
        void b() {
            CommentCreateRequest request = new CommentCreateRequest("테스트 게시물");

            assertThatThrownBy(() -> commentManager.create(request, 1L, 1L))
                .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("댓글 업데이트")
    class UpdateComment {
        @DisplayName("")
        @Test
        void a() {
            Post post = createPost();
            createComment(post.getId());
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            Comment comment = commentManager.update(request, 1L, 1L);

            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getContent().text()).isEqualTo("업데이트 댓글");
            assertThat(comment.getPostId()).isNotNull();
            assertThat(comment.getMemberId()).isNotNull();
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
        }

        @DisplayName("")
        @Test
        void b() {
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            assertThatThrownBy(() -> commentManager.update(request, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        }
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