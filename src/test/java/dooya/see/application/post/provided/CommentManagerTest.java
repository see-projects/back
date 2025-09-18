package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.exception.CommentNotFoundException;
import dooya.see.domain.post.exception.InvalidCommentStatusException;
import dooya.see.domain.post.exception.PostNotFoundException;
import dooya.see.domain.post.exception.UnauthorizedCommentAccessException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record CommentManagerTest(CommentManager commentManager, CommentFinder commentFinder, CommentRepository commentRepository, PostManager postManager, PostFinder postFinder, EntityManager entityManager) {
    @Nested
    class 댓글_생성 {
        @Test
        void 댓글_생성이_성공한다() {
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

        @Test
        void 존재하지_않는_게시글에_댓글_생성_시_예외가_발생한다() {
            CommentCreateRequest request = new CommentCreateRequest("테스트 댓글");

            assertThatThrownBy(() -> commentManager.create(request, 999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    class 댓글_업데이트 {
        @Test
        void 댓글_업데이트가_성공한다() {
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

        @Test
        void 존재하지_않는_댓글_업데이트_시_예외가_발생한다() {
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            assertThatThrownBy(() -> commentManager.update(request, 999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        void 다른_사용자의_댓글_업데이트_시_예외가_발생한다() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            CommentUpdateRequest request = new CommentUpdateRequest("업데이트 댓글");

            assertThatThrownBy(() -> commentManager.update(request, comment.getId(), 2L))
                .isInstanceOf(UnauthorizedCommentAccessException.class);
        }
    }

    @Nested
    class 댓글_삭제 {
        @Test
        void 댓글_삭제가_성공한다() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            Comment deletedComment = commentManager.delete(comment.getId(), 1L);

            assertThat(deletedComment.getId()).isEqualTo(comment.getId());
            assertThat(deletedComment.getStatus()).isEqualTo(CommentStatus.DELETED);
        }

        @Test
        void 존재하지_않는_댓글_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> commentManager.delete(999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        void 이미_삭제된_댓글을_다시_삭제_시_예외가_발생한다() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            commentManager.delete(comment.getId(), 1L);

            assertThatThrownBy(() -> commentManager.delete(comment.getId(), 1L))
                .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void 다른_사용자의_댓글_삭제_시_예외가_발생한다() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            assertThatThrownBy(() -> commentManager.delete(comment.getId(), 2L))
                .isInstanceOf(UnauthorizedCommentAccessException.class);
        }
    }

    @Nested
    class 댓글_숨김 {
        @Test
        void 댓글_숨김이_성공한다() {
            Post post = createPost();
            Comment comment = createComment(post.getId());

            Comment hiddenComment = commentManager.hide(comment.getId(), 1L);

            assertThat(hiddenComment.getId()).isEqualTo(comment.getId());
            assertThat(hiddenComment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
        }

        @Test
        void 존재하지_않는_댓글_숨김_시_예외가_발생한다() {
            assertThatThrownBy(() -> commentManager.hide(999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        void 이미_숨김_처리된_댓글을_다시_숨김_시_예외가_발생한다() {
            Post post = createPost();
            Comment comment = createComment(post.getId());
            commentManager.hide(comment.getId(), 1L);

            assertThatThrownBy(() -> commentManager.hide(comment.getId(), 1L))
                .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void 다른_사용자의_댓글_숨김_시_예외가_발생한다() {
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
