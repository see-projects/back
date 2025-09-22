package dooya.see.application.post.required;

import dooya.see.domain.post.PostStats;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * 게시물 통계 저장소 Secondary Port
 */
public interface PostStatsRepository extends Repository<PostStats, Long> {
    PostStats save(PostStats postStats);

    Optional<PostStats> findByPostId(Long postId);

    long count();
}