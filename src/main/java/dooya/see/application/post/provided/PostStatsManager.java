package dooya.see.application.post.provided;

import dooya.see.domain.post.PostStats;

import java.util.Optional;

/**
 * 게시물 통계 관리를 위한 Primary Port
 */
public interface PostStatsManager {
    void initializePostStats(Long postId);

    void incrementViewCount(Long postId);

    void incrementLikeCount(Long postId);

    void decrementLikeCount(Long postId);

    void incrementCommentCount(Long postId);

    void decrementCommentCount(Long postId);

    Optional<PostStats> getPostStats(Long postId);

    PostStats getPostStatsOrThrow(Long postId);
}
