package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;
import java.util.List;

/**
 * 댓글 조회를 위한 주요 Port
 */
public interface CommentFinder {
    /**
     * 주어진 ID를 통해 댓글을 조회합니다.
     *
     * @param commentId 조회할 댓글의 ID
     * @return ID에 해당하는 댓글 객체
     */
    Comment find(Long commentId);

    /**
     * 주어진 게시물 ID에 해당하는 댓글 리스트를 조회합니다.
     *
     * @param postId 댓글을 조회할 게시물의 ID
     * @return 게시물 ID에 해당하는 댓글 리스트
     */
    List<Comment> findByPostId(Long postId);

    /**
     * 부모 댓글 ID를 기준으로 대댓글(답글) 리스트를 조회합니다.
     *
     * @param parentCommentId 조회할 부모 댓글의 ID
     * @return 주어진 부모 댓글 ID에 해당하는 대댓글 리스트
     */
    List<Comment> findRepliesByParentId(Long parentCommentId);

    /**
     * 회원 ID를 기준으로 댓글 리스트를 조회합니다.
     *
     * @param memberId 댓글을 조회할 회원의 ID
     * @return 주어진 회원 ID와 연관된 댓글 리스트
     */
    List<Comment> findByMemberId(Long memberId);
}