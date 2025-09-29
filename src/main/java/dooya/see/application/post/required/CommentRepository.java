package dooya.see.application.post.required;

import dooya.see.domain.post.Comment;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 데이터 처리를 위한 저장소 인터페이스
 */
public interface CommentRepository extends Repository<Comment, Long> {
    /**
     * 댓글을 저장하는 메서드
     *
     * @param comment 저장할 댓글 객체
     **/
    Comment save(Comment comment);

    /**
     * ID에 해당하는 댓글을 검색합니다.
     *
     * @param id 검색할 댓글의 고유 ID
     * @return 검색된 댓글 정보를 담은 Optional 객체. 해당 ID의 댓글이 없으면 비어 있는 Optional 반환
     */
    Optional<Comment> findById(Long id);

    /**
     * 지정된 게시글 ID에 해당하는 모든 댓글을 검색합니다.
     *
     * @param postId 검색할 게시글의 ID
     * @return 게시글 ID에 연결된 댓글 목록
     */
    List<Comment> findByPostId(Long postId);

    /**
     * 지정된 부모 댓글 ID에 연결된 모든 댓글을 검색합니다.
     *
     * @param parentCommentId 부모 댓글의 고유 ID
     * @return 부모 댓글 ID에 연결된 댓글 목록
     */
    List<Comment> findByParentCommentId(Long parentCommentId);

    /**
     * 지정된 회원 ID에 해당하는 모든 댓글을 검색합니다.
     *
     * @param memberId 댓글을 작성한 회원의 고유 ID
     * @return 회원 ID에 연결된 댓글 목록
     */
    List<Comment> findByMemberId(Long memberId);
}