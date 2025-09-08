package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;
import java.util.List;

public interface CommentFinder {
    Comment find(Long commentId);

    List<Comment> findByPostId(Long postId);

    List<Comment> findRepliesByParentId(Long parentCommentId);

    List<Comment> findByMemberId(Long memberId);
}
