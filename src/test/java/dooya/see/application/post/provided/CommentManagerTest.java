package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.exception.CommentNotFoundException;
import dooya.see.domain.post.exception.InvalidCommentStatusException;
import dooya.see.domain.post.exception.PostNotFoundException;
import dooya.see.domain.post.exception.UnauthorizedCommentAccessException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
    private static final Long AUTHOR_ID = 1L;
    private static final Long UNAUTHORIZED_USER_ID = 2L;
    private static final String COMMENT_CONTENT = "테스트 댓글";
    private static final String UPDATED_CONTENT = "업데이트 댓글";

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 댓글_생성 {
        @Test
        void 댓글_생성이_성공한다() {
            Post post = createTestPost();

            Comment comment = createTestComment(post.getId());

            assertThatCommentCreated(comment, post.getId());
        }

        @Test
        void 존재하지_않는_게시글에_댓글_생성_시_예외가_발생한다() {
            CommentCreateRequest request = new CommentCreateRequest(COMMENT_CONTENT);

            assertThatThrownBy(() -> commentManager.create(request, 999L, AUTHOR_ID))
                    .isInstanceOf(PostNotFoundException.class);
        }

        private void assertThatCommentCreated(Comment comment, Long expectedPostId) {
            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getContent().text()).isEqualTo(COMMENT_CONTENT);
            assertThat(comment.getPostId()).isEqualTo(expectedPostId);
            assertThat(comment.getMemberId()).isEqualTo(AUTHOR_ID);
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNull();
        }
    }

    @Nested
    class 댓글_수정 {
        @Test
        void 댓글_수정이_성공한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);

            Comment updatedComment = commentManager.update(request, comment.getId(), AUTHOR_ID);

            assertThatCommentUpdated(updatedComment, comment, post.getId());
        }

        @Test
        void 존재하지_않는_댓글_수정_시_예외가_발생한다() {
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);

            assertThatThrownBy(() -> commentManager.update(request, 999L, AUTHOR_ID))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        void 다른_사용자의_댓글_수정_시_예외가_발생한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);

            assertThatThrownBy(() -> commentManager.update(request, comment.getId(), UNAUTHORIZED_USER_ID))
                    .isInstanceOf(UnauthorizedCommentAccessException.class);
        }

        private void assertThatCommentUpdated(Comment updatedComment, Comment originalComment, Long expectedPostId) {
            assertThat(updatedComment.getId()).isEqualTo(originalComment.getId());
            assertThat(updatedComment.getContent().text()).isEqualTo(UPDATED_CONTENT);
            assertThat(updatedComment.getPostId()).isEqualTo(expectedPostId);
            assertThat(updatedComment.getMemberId()).isEqualTo(AUTHOR_ID);
            assertThat(updatedComment.getParentCommentId()).isNull();
            assertThat(updatedComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(updatedComment.getMetaData().createdAt()).isNotNull();
            assertThat(updatedComment.getMetaData().modifiedAt()).isNotNull();
        }
    }

    @Nested
    class 댓글_삭제 {
        @Test
        void 댓글_삭제가_성공한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());

            Comment deletedComment = commentManager.delete(comment.getId(), AUTHOR_ID);

            assertThatCommentDeleted(deletedComment, comment.getId());
        }

        @Test
        void 존재하지_않는_댓글_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> commentManager.delete(999L, AUTHOR_ID))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        void 이미_삭제된_댓글을_다시_삭제_시_예외가_발생한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());
            commentManager.delete(comment.getId(), AUTHOR_ID);

            assertThatThrownBy(() -> commentManager.delete(comment.getId(), AUTHOR_ID))
                    .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void 다른_사용자의_댓글_삭제_시_예외가_발생한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());

            assertThatThrownBy(() -> commentManager.delete(comment.getId(), UNAUTHORIZED_USER_ID))
                    .isInstanceOf(UnauthorizedCommentAccessException.class);
        }

        private void assertThatCommentDeleted(Comment deletedComment, Long expectedCommentId) {
            assertThat(deletedComment.getId()).isEqualTo(expectedCommentId);
            assertThat(deletedComment.getStatus()).isEqualTo(CommentStatus.DELETED);
        }
    }

    @Nested
    class 댓글_숨김 {
        @Test
        void 댓글_숨김이_성공한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());

            Comment hiddenComment = commentManager.hide(comment.getId(), AUTHOR_ID);

            assertThatCommentHidden(hiddenComment, comment.getId());
        }

        @Test
        void 존재하지_않는_댓글_숨김_시_예외가_발생한다() {
            assertThatThrownBy(() -> commentManager.hide(999L, AUTHOR_ID))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        void 이미_숨김_처리된_댓글을_다시_숨김_시_예외가_발생한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());
            commentManager.hide(comment.getId(), AUTHOR_ID);

            assertThatThrownBy(() -> commentManager.hide(comment.getId(), AUTHOR_ID))
                    .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void 다른_사용자의_댓글_숨김_시_예외가_발생한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId());

            assertThatThrownBy(() -> commentManager.hide(comment.getId(), UNAUTHORIZED_USER_ID))
                    .isInstanceOf(UnauthorizedCommentAccessException.class);
        }

        private void assertThatCommentHidden(Comment hiddenComment, Long expectedCommentId) {
            assertThat(hiddenComment.getId()).isEqualTo(expectedCommentId);
            assertThat(hiddenComment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
        }
    }

    // 헬퍼 메서드들

    private Post createTestPost() {
        Post post = postManager.create(createPostRequest(), AUTHOR_ID);
        flushAndClearContext();
        return postFinder.find(post.getId());
    }

    private Comment createTestComment(Long postId) {
        Comment comment = commentManager.create(CommentFixture.createCommentRequest(), postId, AUTHOR_ID);
        flushAndClearContext();
        return commentFinder.find(comment.getId());
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}