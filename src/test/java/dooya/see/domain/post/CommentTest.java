package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
}
