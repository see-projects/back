package dooya.see.application.post.required;

import dooya.see.domain.post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 게시물 좋아요 데이터 처리를 위한 저장소 인터페이스
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    /**
     * 지정된 게시물 ID와 회원 ID에 해당하는 좋아요가 존재하는지 확인합니다.
     *
     * @param postId 확인하려는 게시물의 고유 ID
     * @param memberId 확인하려는 회원의 고유 ID
     * @return 좋아요가 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 지정된 게시물 ID와 회원 ID에 해당하는 좋아요 데이터를 삭제합니다.
     *
     * @param postId 삭제하려는 게시물의 고유 ID
     * @param memberId 삭제하려는 좋아요를 작성한 회원의 고유 ID
     */
    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 지정된 게시물 ID에 해당하는 좋아요 개수를 반환합니다.
     *
     * @param postId 좋아요 개수를 확인하려는 게시물의 고유 ID
     * @return 해당 게시물의 좋아요 개수
     */
    long countByPostId(Long postId);
}