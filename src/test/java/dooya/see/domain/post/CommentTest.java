package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {
    @DisplayName("")
    @Test
    void createTopLevelComment () {
        CommentCreateRequest request = new CommentCreateRequest("content");
        Long postId = 1L;
        Long memberId = 1L;

        Comment comment = Comment.create(request, postId, memberId);

        assertThat(comment.getContent().text()).isEqualTo(request.body());
        assertThat(comment.getPostId()).isEqualTo(postId);
        assertThat(comment.getMemberId()).isEqualTo(memberId);
        assertThat(comment.getParentCommentId()).isNull();
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(comment.getMetaData().createdAt()).isNotNull();
    }
}
