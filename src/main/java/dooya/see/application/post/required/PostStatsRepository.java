package dooya.see.application.post.required;

import dooya.see.domain.post.PostStats;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * PostStats 엔티티에 대한 데이터 액세스를 담당하는 레포지토리 인터페이스
 */
public interface PostStatsRepository extends Repository<PostStats, Long> {
    /**
     * PostStats 객체를 저장합니다.
     *
     * @param postStats 저장할 PostStats 객체
     * @return 저장된 PostStats 객체
     */
    PostStats save(PostStats postStats);

    /**
     * 주어진 게시물 ID에 해당하는 PostStats를 조회합니다.
     *
     * @param postId 조회할 게시물의 고유 ID
     * @return 지정된 게시물 ID를 가진 PostStats의 Optional 객체. PostStats가 없을 경우 빈 Optional 반환
     */
    Optional<PostStats> findByPostId(Long postId);

    /**
     * 저장된 PostStats 엔티티의 총 개수를 반환합니다.
     *
     * @return 저장된 PostStats 엔티티의 총 개수
     */
    long count();
}