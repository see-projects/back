package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentCreateRequest;

public interface CommentManager {
    Comment create(CommentCreateRequest request, Long postId, Long memberId);
}
