package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.post.exception.EmptyCommentUpdateException;
import dooya.see.domain.post.exception.InvalidCommentStatusException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest {
    private static final Long POST_ID = 1L;
    private static final Long MEMBER_ID = 1L;
    private static final Long PARENT_COMMENT_ID = 10L;
    private static final String VALID_CONTENT = "좋은 글이네요!";
    private static final String REPLY_CONTENT = "답글입니다!";
    private static final String UPDATED_CONTENT = "수정된 댓글입니다!";
    private static final String LONG_CONTENT = "a".repeat(1001);

    @Nested
    class 댓글_생성 {
        @Test
        void 일반_댓글_생성_시_모든_필드가_올바르게_설정된다() {
            CommentCreateRequest request = new CommentCreateRequest(VALID_CONTENT);

            Comment comment = Comment.create(request, POST_ID, MEMBER_ID);

            assertThatTopLevelCommentCreated(comment);
        }

        @Test
        void 답글_생성_시_부모_댓글_ID가_올바르게_설정된다() {
            CommentCreateRequest request = new CommentCreateRequest(REPLY_CONTENT, PARENT_COMMENT_ID);

            Comment reply = Comment.create(request, POST_ID, MEMBER_ID);

            assertThatReplyCommentCreated(reply);
        }

        @Test
        void null_내용으로_댓글_생성_시_예외가_발생한다() {
            CommentCreateRequest request = new CommentCreateRequest(null);

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 빈_내용으로_댓글_생성_시_예외가_발생한다() {
            CommentCreateRequest request = new CommentCreateRequest("  ");

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 길이_제한_초과_내용으로_댓글_생성_시_예외가_발생한다() {
            CommentCreateRequest request = new CommentCreateRequest(LONG_CONTENT);

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private void assertThatTopLevelCommentCreated(Comment comment) {
            assertThat(comment.getContent().text()).isEqualTo(VALID_CONTENT);
            assertThat(comment.getPostId()).isEqualTo(POST_ID);
            assertThat(comment.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNull();
            assertThat(comment.isReply()).isFalse();
            assertThat(comment.isTopLevel()).isTrue();
        }

        private void assertThatReplyCommentCreated(Comment reply) {
            assertThat(reply.getContent().text()).isEqualTo(REPLY_CONTENT);
            assertThat(reply.getPostId()).isEqualTo(POST_ID);
            assertThat(reply.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(reply.getParentCommentId()).isEqualTo(PARENT_COMMENT_ID);
            assertThat(reply.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(reply.isReply()).isTrue();
            assertThat(reply.isTopLevel()).isFalse();
        }
    }

    @Nested
    class 댓글_수정 {
        @Test
        void 댓글_수정_시_내용과_수정일시가_변경되고_이벤트가_발생한다() {
            Comment comment = createActiveComment();
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);

            comment.update(request);

            assertThatCommentUpdated(comment);
            assertThatCommentUpdatedEventOccurred(comment);
        }

        @Test
        void 삭제된_댓글은_수정할_수_없다() {
            Comment comment = createDeletedComment();
            CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);

            assertThatThrownBy(() -> comment.update(request))
                    .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void null_내용으로_댓글_수정_시_예외가_발생한다() {
            Comment comment = createActiveComment();
            CommentUpdateRequest request = new CommentUpdateRequest(null);

            assertThatThrownBy(() -> comment.update(request))
                    .isInstanceOf(EmptyCommentUpdateException.class);
        }

        private void assertThatCommentUpdated(Comment comment) {
            assertThat(comment.getContent().text()).isEqualTo(UPDATED_CONTENT);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
        }

        private void assertThatCommentUpdatedEventOccurred(Comment comment) {
            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentUpdated.class);
        }
    }

    @Nested
    class 댓글_삭제 {
        @Test
        void 댓글_삭제_시_상태_변경과_이벤트가_발생한다() {
            Comment comment = createActiveComment();

            comment.delete();

            assertThatCommentDeleted(comment);
            assertThatCommentDeletedEventOccurred(comment);
        }

        @Test
        void 이미_삭제된_댓글을_다시_삭제할_수_없다() {
            Comment comment = createDeletedComment();

            assertThatThrownBy(comment::delete)
                    .isInstanceOf(InvalidCommentStatusException.class);
        }

        private void assertThatCommentDeleted(Comment comment) {
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isDeleted()).isTrue();
        }

        private void assertThatCommentDeletedEventOccurred(Comment comment) {
            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentDeleted.class);
        }
    }

    @Nested
    class 댓글_숨김 {
        @Test
        void 댓글_숨김_시_상태_변경과_이벤트가_발생한다() {
            Comment comment = createActiveComment();

            comment.hide();

            assertThatCommentHidden(comment);
            assertThatCommentHiddenEventOccurred(comment);
        }

        @Test
        void 이미_숨김_처리된_댓글을_다시_숨길_수_없다() {
            Comment comment = createHiddenComment();

            assertThatThrownBy(comment::hide)
                    .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void 삭제된_댓글은_숨김_처리할_수_없다() {
            Comment comment = createDeletedComment();

            assertThatThrownBy(comment::hide)
                    .isInstanceOf(InvalidCommentStatusException.class);
        }

        private void assertThatCommentHidden(Comment comment) {
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isHidden()).isTrue();
        }

        private void assertThatCommentHiddenEventOccurred(Comment comment) {
            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentHidden.class);
        }
    }

    @Nested
    class 권한_검증 {
        @Test
        void 작성자_확인이_올바르게_동작한다() {
            Comment comment = createActiveComment();

            assertThat(comment.isWrittenBy(MEMBER_ID)).isTrue();
            assertThat(comment.isWrittenBy(MEMBER_ID + 1)).isFalse();
        }

        @Test
        void 활성_댓글은_수정_가능하다() {
            Comment comment = createActiveComment();

            assertThat(comment.canBeModified()).isTrue();
        }

        @Test
        void 삭제된_댓글은_수정할_수_없다() {
            Comment comment = createDeletedComment();

            assertThat(comment.canBeModified()).isFalse();
        }

        @Test
        void 숨겨진_댓글도_수정할_수_없다() {
            Comment comment = createHiddenComment();

            assertThat(comment.canBeModified()).isFalse();
        }
    }

    @Nested
    class 대댓글_관련 {
        @Test
        void 일반_댓글은_최상위_댓글이다() {
            Comment comment = createActiveComment();

            assertThat(comment.isReply()).isFalse();
            assertThat(comment.isTopLevel()).isTrue();
        }

        @Test
        void 부모_댓글_ID가_있는_댓글은_답글이다() {
            CommentCreateRequest request = new CommentCreateRequest(VALID_CONTENT, PARENT_COMMENT_ID);
            Comment reply = Comment.create(request, POST_ID, MEMBER_ID);

            assertThat(reply.isReply()).isTrue();
            assertThat(reply.isTopLevel()).isFalse();
        }
    }

    // 헬퍼 메서드들
    private Comment createActiveComment() {
        CommentCreateRequest request = new CommentCreateRequest(VALID_CONTENT);
        Comment comment = Comment.create(request, POST_ID, MEMBER_ID);
        comment.clearDomainEvents();

        return comment;
    }

    private Comment createDeletedComment() {
        Comment comment = createActiveComment();
        comment.delete();
        comment.clearDomainEvents(); // 이벤트 클리어로 테스트 격리
        return comment;
    }

    private Comment createHiddenComment() {
        Comment comment = createActiveComment();
        comment.hide();
        comment.clearDomainEvents(); // 이벤트 클리어로 테스트 격리
        return comment;
    }
}