package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;
import java.util.List;

/**
 * 댓글 조회 Primary Port
 */
public interface CommentFinder {
    Comment find(Long commentId);

    List<Comment> findByPostId(Long postId);

    List<Comment> findRepliesByParentId(Long parentCommentId);

    List<Comment> findByMemberId(Long memberId);
}