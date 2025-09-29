package dooya.see.domain.post;

import dooya.see.domain.post.dto.CommentCreateRequest;
import dooya.see.domain.post.dto.CommentUpdateRequest;

public class CommentFixture {

    public static CommentCreateRequest createCommentRequest() {
        return new CommentCreateRequest("테스트 댓글");
    }

    public static CommentCreateRequest createCommentRequest(String content) {
        return new CommentCreateRequest(content);
    }

    public static CommentCreateRequest createReplyRequest(String content, Long parentCommentId) {
        return new CommentCreateRequest(content, parentCommentId);
    }

    public static CommentUpdateRequest createCommentUpdateRequest() {
        return new CommentUpdateRequest("수정된 댓글 내용");
    }

    public static CommentUpdateRequest createCommentUpdateRequest(String content) {
        return new CommentUpdateRequest(content);
    }
}
