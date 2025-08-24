package dooya.see.application.post.required;

import dooya.see.domain.post.PostStats;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface PostStatsRepository extends Repository<PostStats, Long> {
    PostStats save(PostStats postStats);

    Optional<PostStats> findByPostId(Long postId);
}
