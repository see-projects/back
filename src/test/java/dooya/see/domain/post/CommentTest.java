package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {
    private static final Long POST_ID = 1L;
    private static final Long MEMBER_ID = 1L;
    private static final Long ANOTHER_MEMBER_ID = 2L;

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
}
