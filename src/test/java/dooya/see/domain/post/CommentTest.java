package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest {
    private static final Long POST_ID = 1L;
    private static final Long MEMBER_ID = 1L;

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @DisplayName("일반 댓글 생성 시 댓글 정보가 올바르게 설정된다")
        @Test
        void createTopLevelComment () {
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

        @DisplayName("답글 댓글 생성 시 부모 댓글 ID가 올바르게 설정된다")
        @Test
        void createReplyComment() {
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

        @DisplayName("null 내용으로 댓글 생성 시 예외가 발생한다")
        @Test
        void createCommentWithNullContent() {
            CommentCreateRequest request = new CommentCreateRequest(null);

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("빈 내용으로 댓글 생성 시 예외가 발생한다")
        @Test
        void createCommentWithEmptyContent() {
            CommentCreateRequest request = new CommentCreateRequest("  ");

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);

        }

        @DisplayName("길이 제한을 초과한 내용으로 댓글 생성 시 예외가 발생한다")
        @Test
        void createCommentWithWhiteTooLongContent() {
            String longContent = "a".repeat(1001);
            CommentCreateRequest request = new CommentCreateRequest(longContent);

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {
        @DisplayName("댓글 수정 시 내용과 수정일시가 변경된다")
        @Test
        void updateComment() {
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

        @DisplayName("삭제된 댓글은 수정할 수 없다")
        @Test
        void cannotUpdateDeleteComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다!");

            assertThatThrownBy(() -> comment.update(request))
                .isInstanceOf(IllegalStateException.class);
        }

        @DisplayName("null 내용으로 댓글 수정 시 예외가 발생한다")
        @Test
        void updateCommentWithNoContent() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            CommentUpdateRequest request = new CommentUpdateRequest(null);

            assertThatThrownBy(() -> comment.update(request))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        @DisplayName("댓글 삭제 시 상태가 DELETED로 변경되고 수정일시가 설정된다")
        @Test
        void deleteComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            comment.delete();

            assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isDelete()).isTrue();

            assertThat(comment.hasDomainEvents()).isTrue();
            assertThat(comment.getDomainEvents()).hasSize(1);
            assertThat(comment.getDomainEvents().get(0)).isInstanceOf(CommentDeleted.class);
        }

        @DisplayName("이미 삭제된 댓글을 다시 삭제할 수 없다")
        @Test
        void cannotDeleteAlreadyDeleteComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();

            assertThatThrownBy(comment::delete)
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("댓글 숨김")
    class HideComment {
        @DisplayName("댓글 숨김 시 상태가 HIDDEN으로 변경되고 수정일시가 설정된다")
        @Test
        void hideComment() {
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

        @DisplayName("이미 숨김 처리된 댓글을 다시 숨길 수 없다")
        @Test
        void cannotHideDeletedComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.hide();

            assertThatThrownBy(comment::hide)
                .isInstanceOf(IllegalStateException.class);
        }

        @DisplayName("삭제된 댓글은 숨김 처리할 수 없다")
        @Test
        void cannotHideDeleteComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();

            assertThatThrownBy(comment::hide)
                    .isInstanceOf(IllegalStateException.class);
        }
    }
    
    @Nested
    @DisplayName("권한 검증")
    class Authorization {
        @DisplayName("작성자 ID와 일치하는 회원이 작성자인지 확인한다")
        @Test
        void isWrittenByOwner() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            
            assertThat(comment.isWrittenBy(MEMBER_ID)).isTrue();
        }
        
        @DisplayName("활성 상태의 댓글은 수정 가능하다")
        @Test
        void canBeModifiedWhenActive() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.canBeModified()).isTrue();
        }

        @DisplayName("삭제된 댓글은 수정할 수 없다")
        @Test
        void cannotBeModifiedWhenDelete() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();

            assertThat(comment.canBeModified()).isFalse();
        }
    }

    @Nested
    @DisplayName("대댓글 관련")
    class ReplyComments {
        @DisplayName("일반 댓글은 답글이 아니고 최상위 댓글이다")
        @Test
        void isReplyForTopLevelComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.isReply()).isFalse();
            assertThat(comment.isTopLevel()).isTrue();
        }

        @DisplayName("부모 댓글 ID가 있는 댓글은 답글이고 최상위 댓글이 아니다")
        @Test
        void isReplyForReplyComment() {
            Long parentCommentId = 10L;
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!", parentCommentId);
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.isReply()).isTrue();
            assertThat(comment.isTopLevel()).isFalse();
        }
    }
}
