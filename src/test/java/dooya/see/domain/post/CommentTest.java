package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest {
    private static final Long POST_ID = 1L;
    private static final Long MEMBER_ID = 1L;
    private static final Long ANOTHER_MEMBER_ID = 2L;

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @DisplayName("")
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

        @DisplayName("")
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

        @DisplayName("")
        @Test
        void createCommentWithNullContent() {
            CommentCreateRequest request = new CommentCreateRequest(null);

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("")
        @Test
        void createCommentWithEmptyContent() {
            CommentCreateRequest request = new CommentCreateRequest("  ");

            assertThatThrownBy(() -> Comment.create(request, POST_ID, MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);

        }

        @DisplayName("")
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
        @DisplayName("")
        @Test
        void updateComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다!");

            comment.update(request);

            assertThat(comment.getContent().text()).isEqualTo("수정된 댓글입니다!");
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
        }

        @DisplayName("")
        @Test
        void cannotUpdateDeleteComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.delete();
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다!");

            assertThatThrownBy(() -> comment.update(request))
                .isInstanceOf(IllegalStateException.class);
        }

        @DisplayName("")
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
        @DisplayName("")
        @Test
        void deleteComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            comment.delete();

            assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isDelete()).isTrue();
        }

        @DisplayName("")
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
        @DisplayName("")
        @Test
        void hideComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            comment.hide();

            assertThat(comment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
            assertThat(comment.getMetaData().modifiedAt()).isNotNull();
            assertThat(comment.isHidden()).isTrue();
        }

        @DisplayName("")
        @Test
        void cannotHideDeletedComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            comment.hide();

            assertThatThrownBy(comment::hide)
                .isInstanceOf(IllegalStateException.class);
        }

        @DisplayName("")
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
        @DisplayName("")
        @Test
        void isWrittenByOwner() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);
            
            assertThat(comment.isWrittenBy(MEMBER_ID)).isTrue();
        }
        
        @DisplayName("")
        @Test
        void canBeModifiedWhenActive() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.canBeModified()).isTrue();
        }

        @DisplayName("")
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
        @DisplayName("")
        @Test
        void isReplyForTopLevelComment() {
            CommentCreateRequest createRequest = new CommentCreateRequest("좋은 글이네요!");
            Comment comment = Comment.create(createRequest, POST_ID, MEMBER_ID);

            assertThat(comment.isReply()).isFalse();
            assertThat(comment.isTopLevel()).isTrue();
        }

        @DisplayName("")
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
