package dooya.see.application.post.provided;

import dooya.see.domain.post.PostStats;

import java.util.Optional;

/**
 * 게시물의 통계 데이터를 관리하기 위한 인터페이스입니다.
 */
public interface PostStatsManager {
    /**
     * 지정된 게시물 ID를 기반으로 통계 데이터를 초기화합니다.
     *
     * @param postId 초기화할 게시물의 식별자
     */
    void initializePostStats(Long postId);

    /**
     * 게시물의 조회수를 1 증가시킵니다.
     *
     * @param postId 조회수를 증가시킬 게시물의 식별자
     */
    void incrementViewCount(Long postId);

    /**
     * 게시물의 좋아요 개수를 1 증가시킵니다.
     *
     * @param postId 좋아요 개수를 증가시킬 게시물의 식별자
     */
    void incrementLikeCount(Long postId);

    /**
     * 게시물의 좋아요 개수를 1 감소시킵니다.
     *
     * @param postId 좋아요 개수를 감소시킬 게시물의 식별자
     */
    void decrementLikeCount(Long postId);

    /**
     * 게시물의 댓글 개수를 1 증가시킵니다.
     *
     * @param postId 댓글 개수를 증가시킬 게시물의 식별자
     */
    void incrementCommentCount(Long postId);

    /**
     * 게시물의 댓글 개수를 1 감소시킵니다.
     *
     * @param postId 댓글 개수를 감소시킬 게시물의 식별자
     */
    void decrementCommentCount(Long postId);

    /**
     * 지정된 게시물 ID를 사용하여 해당 게시물의 통계 데이터를 반환합니다.
     *
     * @param postId 통계 데이터를 조회할 게시물의 식별자
     * @return 지정된 게시물의 통계 데이터를 포함하는 Optional 객체
     */
    Optional<PostStats> getPostStats(Long postId);

    /**
     * 지정된 게시물 ID에 해당하는 통계 데이터를 반환합니다.
     * 통계 데이터가 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @param postId 통계 데이터를 조회할 게시물의 식별자
     * @return 지정된 게시물의 통계 데이터
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     */
    PostStats getPostStatsOrThrow(Long postId);
}
