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

    @Nested
    class 댓글_생성 {
        @Test
        void 일반_댓글_생성_시_댓글_정보가_올바르게_설정된다() {
            CommentCreateRequest request = new CommentCreateRequest("좋은 글이네요!");

            Comment comment = Comment.create(request, POST_ID, MEMBER_ID);

            assertThat(comment.getContent().text()).isEqualTo(request.body());
            assertThat(comment.getPostId()).isEqualTo(POST_ID);
            assertThat(comment.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNull();
            assertThat(comment.isReply()).isFalse();
            assertThat(comment.isTopLevel()).isTrue();
        }

        @Test
        void 답글_댓글_생성_시_부모_댓글_ID가_올바르게_설정된다() {
            Long parentCommentId = 1L;
            CommentCreateRequest request = new CommentCreateRequest("답글입니다!", parentCommentId);

            Comment reply = Comment.create(request, POST_ID, MEMBER_ID);

            assertThat(reply.getContent().text()).isEqualTo("답글입니다!");
            assertThat(reply.getPostId()).isEqualTo(POST_ID);
            assertThat(reply.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(reply.getParentCommentId()).isEqualTo(parentCommentId);
            assertThat(reply.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(reply.isReply()).isTrue();
            assertThat(reply.isTopLevel()).isFalse();
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
        void 길이_제한을_초과한_내용으로_댓글_생성_시_예외가_발생한다() {
            String longContent = "a".repeat(1001);
            CommentCreateRequest request = new CommentCreateRequest(longContent);

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class 댓글_수정 {
        @Test
        void 댓글_수정_시_내용과_수정일시가_변경된다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다!");

            comment.update(request);

            assertThat(comment.getContent().text()).isEqualTo("수정된 댓글입니다!");
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();

            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentUpdated.class);
        }

        @Test
        void 삭제된_댓글은_수정할_수_없다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다!");

            assertThatThrownBy(() -> comment.update(request))
                .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void null_내용으로_댓글_수정_시_예외가_발생한다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            CommentUpdateRequest request = new CommentUpdateRequest(null);

            assertThatThrownBy(() -> comment.update(request))
                .isInstanceOf(EmptyCommentUpdateException.class);
        }
    }

    @Nested
    class 댓글_삭제 {
        @Test
        void 댓글_삭제_시_상태가_DELETED로_변경되고_수정일시가_설정된다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            comment.delete();

            assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isDeleted()).isTrue();

            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentDeleted.class);
        }

        @Test
        void 이미_삭제된_댓글을_다시_삭제할_수_없다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();

            assertThatThrownBy(comment::delete)
                .isInstanceOf(InvalidCommentStatusException.class);
        }
    }

    @Nested
    class 댓글_숨김 {
        @Test
        void 댓글_숨김_시_상태가_HIDDEN으로_변경되고_수정일시가_설정된다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            comment.hide();

            assertThat(comment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isHidden()).isTrue();

            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentHidden.class);
        }

        @Test
        void 이미_숨김_처리된_댓글을_다시_숨길_수_없다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.hide();

            assertThatThrownBy(comment::hide)
                .isInstanceOf(InvalidCommentStatusException.class);
        }

        @Test
        void 삭제된_댓글은_숨김_처리할_수_없다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();

            assertThatThrownBy(comment::hide)
                    .isInstanceOf(InvalidCommentStatusException.class);
        }
    }
    
    @Nested
    class 권한_검증 {
        @Test
        void 작성자_ID와_일치하는_회원이_작성자인지_확인한다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            
            assertThat(comment.isWrittenBy(MEMBER_ID)).isTrue();
        }

        @Test
        void 활성_상태의_댓글은_수정_가능하다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.canBeModified()).isTrue();
        }

        @Test
        void 삭제된_댓글은_수정할_수_없다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();

            assertThat(comment.canBeModified()).isFalse();
        }
    }

    @Nested
    class 대댓글_관련 {
        @Test
        void 일반_댓글은_답글이_아니고_최상위_댓글이다() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.isReply()).isFalse();
            assertThat(comment.isTopLevel()).isTrue();
        }

        @Test
        void 부모_댓글_ID가_있는_댓글은_답글이고_최상위_댓글이_아니다() {
            Long parentCommentId = 10L;
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!", parentCommentId);
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.isReply()).isTrue();
            assertThat(comment.isTopLevel()).isFalse();
        }
    }
}
