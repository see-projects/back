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
        @DisplayName("댓글 생성이 성공한다")
        @Test
        void createCommentSuccess() {
            Post post = createPost();
            
            Comment comment = createComment(post.getId());

            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getContent().text()).isEqualTo("테스트 댓글");
            assertThat(comment.getPostId()).isEqualTo(post.getId());
            assertThat(comment.getMemberId()).isEqualTo(1L);
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNull();
        }

        @DisplayName("존재하지 않는 게시글에 댓글 생성 시 예외가 발생한다")
        @Test
        void createCommentWithNonexistentPostThrowsException() {
            CommentCreateRequest request = new CommentCreateRequest("테스트 댓글");

            assertThatThrownBy(() -> commentManager.create(request, 999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("댓글 업데이트")
    class UpdateComment {
        @DisplayName("댓글 업데이트가 성공한다")
        @Test
        void updateCommentSuccess() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            Comment updatedComment = commentManager.update(request, comment.getId(), 1L);

            assertThat(updatedComment.getId()).isEqualTo(comment.getId());
            assertThat(updatedComment.getContent().text()).isEqualTo("업데이트 댓글");
            assertThat(updatedComment.getPostId()).isEqualTo(post.getId());
            assertThat(updatedComment.getMemberId()).isEqualTo(1L);
            assertThat(updatedComment.getParentCommentId()).isNull();
            assertThat(updatedComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(updatedComment.getMetaData().createdAt()).isNotNull();
            assertThat(updatedComment.getMetaData().modifiedAt()).isNotNull();
        }

        @DisplayName("존재하지 않는 댓글 업데이트 시 예외가 발생한다")
        @Test
        void updateNonexistentCommentThrowsException() {
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            assertThatThrownBy(() -> commentManager.update(request, 999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
        }

        @DisplayName("다른 사용자의 댓글 업데이트 시 예외가 발생한다")
        @Test
        void updateOthersCommentThrowsException() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            assertThatThrownBy(() -> commentManager.update(request, comment.getId(), 2L))
                .isInstanceOf(UnauthorizedCommentAccessException.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        @DisplayName("댓글 삭제가 성공한다")
        @Test
        void deleteCommentSuccess() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            Comment deletedComment = commentManager.delete(comment.getId(), 1L);

            assertThat(deletedComment.getId()).isEqualTo(comment.getId());
            assertThat(deletedComment.getStatus()).isEqualTo(CommentStatus.DELETED);
        }

        @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
        @Test
        void deleteNonexistentCommentThrowsException() {
            assertThatThrownBy(() -> commentManager.delete(999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
        }

        @DisplayName("이미 삭제된 댓글을 다시 삭제 시 예외가 발생한다")
        @Test
        void deleteAlreadyDeletedCommentThrowsException() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            commentManager.delete(comment.getId(), 1L);

            assertThatThrownBy(() -> commentManager.delete(comment.getId(), 1L))
                .isInstanceOf(InvalidCommentStatusException.class);
        }

        @DisplayName("다른 사용자의 댓글 삭제 시 예외가 발생한다")
        @Test
        void deleteOthersCommentThrowsException() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            assertThatThrownBy(() -> commentManager.delete(comment.getId(), 2L))
                .isInstanceOf(UnauthorizedCommentAccessException.class);
        }
    }

    @Nested
    @DisplayName("댓글 숨김")
    class HideComment {
        @DisplayName("댓글 숨김이 성공한다")
        @Test
        void hideCommentSuccess() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            Comment hiddenComment = commentManager.hide(comment.getId(), 1L);

            assertThat(hiddenComment.getId()).isEqualTo(comment.getId());
            assertThat(hiddenComment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
        }

        @DisplayName("존재하지 않는 댓글 숨김 시 예외가 발생한다")
        @Test
        void hideNonexistentCommentThrowsException() {
            assertThatThrownBy(() -> commentManager.hide(999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
        }

        @DisplayName("이미 숨김 처리된 댓글을 다시 숨김 시 예외가 발생한다")
        @Test
        void hideAlreadyHiddenCommentThrowsException() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            commentManager.hide(comment.getId(), 1L);

            assertThatThrownBy(() -> commentManager.hide(comment.getId(), 1L))
                .isInstanceOf(InvalidCommentStatusException.class);
        }

        @DisplayName("다른 사용자의 댓글 숨김 시 예외가 발생한다")
        @Test
        void hideOthersCommentThrowsException() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            assertThatThrownBy(() -> commentManager.hide(comment.getId(), 2L))
                .isInstanceOf(UnauthorizedCommentAccessException.class);
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
