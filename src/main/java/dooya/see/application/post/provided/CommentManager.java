package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;
import dooya.see.domain.post.dto.CommentCreateRequest;
import dooya.see.domain.post.dto.CommentUpdateRequest;

/**
 * 댓글 생성, 수정, 삭제, 숨김 등의 관리 기능을 제공하는 인터페이스
 */
public interface CommentManager {
    /**
     * 새로운 댓글을 생성합니다.
     *
     * @param request 댓글 생성 요청 정보
     * @param postId 댓글이 작성될 게시글의 ID
     * @param memberId 댓글 작성자의 회원 ID
     * @return 생성된 댓글 객체
     */
    Comment create(CommentCreateRequest request, Long postId, Long memberId);

    /**
     * 댓글을 수정합니다.
     *
     * @param request 댓글 수정 요청 정보
     * @param commentId 수정할 댓글의 ID
     * @param memberId 댓글 작성자의 회원 ID
     * @return 수정된 댓글 객체
     * @throws dooya.see.domain.post.exception.InvalidCommentStatusException 댓글이 수정 가능한 상태가 아닌 경우
     * @throws dooya.see.domain.post.exception.EmptyCommentUpdateException 수정 요청 정보가 비어 있는 경우
     */
    Comment update(CommentUpdateRequest request, Long commentId, Long memberId);

    /**
     * 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글의 ID
     * @param memberId 댓글 작성자의 회원 ID
     * @return 삭제 처리된 댓글 객체
     * @throws dooya.see.domain.post.exception.InvalidCommentStatusException 댓글이 삭제 가능한 상태가 아닌 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedCommentAccessException 댓글 작성자와 요청자가 일치하지 않는 경우
     */
    Comment delete(Long commentId, Long memberId);

    /**
     * 댓글을 숨깁니다.
     *
     * @param commentId 숨길 댓글의 ID
     * @param memberId 댓글 작성자의 회원 ID
     * @return 숨김 처리된 댓글 객체
     * @throws dooya.see.domain.post.exception.InvalidCommentStatusException 댓글이 숨길 수 없는 상태인 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedCommentAccessException 댓글 작성자와 요청자가 일치하지 않는 경우
     */
    Comment hide(Long commentId, Long memberId);
}