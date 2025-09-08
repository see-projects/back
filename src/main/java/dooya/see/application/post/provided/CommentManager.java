package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentCreateRequest;
import dooya.see.domain.post.CommentUpdateRequest;

public interface CommentManager {
    Comment create(CommentCreateRequest request, Long postId, Long memberId);

    Comment update(CommentUpdateRequest request, Long commentId, Long memberId);

    Comment delete(Long commentId, Long memberId);

    Comment hide(Long commentId, Long memberId);
}
